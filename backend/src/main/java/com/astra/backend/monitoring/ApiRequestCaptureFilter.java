
package com.astra.backend.monitoring;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ApiRequestCaptureFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(ApiRequestCaptureFilter.class);

    private static final int MAX_BODY_BYTES = 8192;

    private final ApiRequestMonitorService monitorService;
    private final ObjectMapper objectMapper;

    public ApiRequestCaptureFilter(
            ApiRequestMonitorService monitorService,
            ObjectMapper objectMapper
    ) {
        this.monitorService = monitorService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // Capture only application API requests.
        if (!uri.startsWith("/api/v1/")) {
            return true;
        }

        // Never monitor the monitoring ingestion endpoint itself.
        if (uri.equals("/api/v1/monitoring/frontend")) {
            return true;
        }

        // Avoid recursive monitoring and sensitive admin-monitor reads.
        if (uri.startsWith("/api/v1/admin/monitoring")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startedAt = System.nanoTime();

        ContentCachingRequestWrapper requestWrapper =
                new ContentCachingRequestWrapper(
                        request,
                        MAX_BODY_BYTES
                );

        BoundedResponseWrapper responseWrapper =
                new BoundedResponseWrapper(
                        response,
                        MAX_BODY_BYTES
                );

        Exception requestFailure = null;

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (IOException | ServletException | RuntimeException exception) {
            requestFailure = exception;
            throw exception;
        } finally {
            long durationMs =
                    (System.nanoTime() - startedAt) / 1_000_000;

            try {
                capture(
                        requestWrapper,
                        responseWrapper,
                        durationMs,
                        requestFailure
                );
            } catch (Exception monitoringException) {
                // Monitoring failures must not break application requests.
                log.warn(
                        "Unable to persist API request monitoring event: {}",
                        monitoringException.getMessage()
                );
            }
        }
    }

    private void capture(
            ContentCachingRequestWrapper request,
            BoundedResponseWrapper response,
            long durationMs,
            Exception failure
    ) {
        int status = response.getStatus();

        String requestId = request.getHeader("X-Request-ID");

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        ApiRequestLog apiLog = ApiRequestLog.create(
                "BACKEND",
                request.getMethod(),
                ApiLogSanitizer.sanitizeEndpoint(request.getRequestURI()),
                status,
                durationMs
        );

        apiLog.setRequestId(requestId);
        apiLog.setRequestTimestamp(
                LocalDateTime.now(ZoneOffset.UTC)
        );

        apiLog.setClientIp(resolveClientIp(request));
        apiLog.setUserAgent(request.getHeader("User-Agent"));

        apiLog.setRequestHeaders(
                serializeSanitizedHeaders(request)
        );

        apiLog.setRequestBody(
                readRequestBody(request)
        );

        apiLog.setResponseHeaders(
                serializeSanitizedResponseHeaders(response)
        );

        apiLog.setResponseBody(
                readResponseBody(response)
        );

        if (failure != null) {
            apiLog.setErrorMessage(
                    ApiLogSanitizer.sanitizeText(
                            failure.getClass().getSimpleName()
                    )
            );
            apiLog.setSuccess(false);
        } else {
            apiLog.setSuccess(status >= 200 && status < 400);
        }

        monitorService.saveBackendLog(apiLog);
    }

    private String serializeSanitizedHeaders(
            HttpServletRequest request
    ) {
        try {
            Map<String, Object> headers = new LinkedHashMap<>();

            Enumeration<String> names = request.getHeaderNames();

            while (names != null && names.hasMoreElements()) {
                String name = names.nextElement();

                Enumeration<String> values =
                        request.getHeaders(name);

                StringBuilder combined = new StringBuilder();

                while (values != null && values.hasMoreElements()) {
                    if (!combined.isEmpty()) {
                        combined.append(", ");
                    }
                    combined.append(values.nextElement());
                }

                headers.put(name, combined.toString());
            }

            JsonNode json = objectMapper.valueToTree(headers);

            return ApiLogSanitizer.sanitizeJson(
                    json,
                    objectMapper
            );
        } catch (Exception exception) {
            return "[HEADERS_UNAVAILABLE]";
        }
    }

    private String serializeSanitizedResponseHeaders(
            HttpServletResponse response
    ) {
        try {
            Map<String, Object> headers = new LinkedHashMap<>();

            Collection<String> names = response.getHeaderNames();

            for (String name : names) {
                headers.put(name, response.getHeaders(name));
            }

            JsonNode json = objectMapper.valueToTree(headers);

            return ApiLogSanitizer.sanitizeJson(
                    json,
                    objectMapper
            );
        } catch (Exception exception) {
            return "[HEADERS_UNAVAILABLE]";
        }
    }

    private String readRequestBody(
            ContentCachingRequestWrapper request
    ) {
        String contentType = request.getContentType();

        if (!isLoggableContentType(contentType)) {
            return null;
        }

        byte[] body = request.getContentAsByteArray();

        if (body.length == 0) {
            return null;
        }

        String text = new String(
                body,
                resolveCharset(request.getCharacterEncoding())
        );

        return sanitizeBodyText(text);
    }

    private String readResponseBody(
            BoundedResponseWrapper response
    ) {
        String contentType = response.getContentType();

        if (!isLoggableContentType(contentType)) {
            return null;
        }

        byte[] body = response.getCapturedBody();

        if (body.length == 0) {
            return null;
        }

        String text = new String(
                body,
                resolveCharset(response.getCharacterEncoding())
        );

        return sanitizeBodyText(text);
    }

    private String sanitizeBodyText(String text) {
        try {
            JsonNode json = objectMapper.readTree(text);

            if (json != null) {
                return ApiLogSanitizer.sanitizeJson(
                        json,
                        objectMapper
                );
            }
        } catch (Exception ignored) {
            // Plain text is sanitized separately.
        }

        return ApiLogSanitizer.sanitizeText(text);
    }

    private boolean isLoggableContentType(String contentType) {
        if (contentType == null) {
            return false;
        }

        String normalized = contentType.toLowerCase();

        return normalized.contains("application/json")
                || normalized.startsWith("text/");
    }

    private Charset resolveCharset(String encoding) {
        try {
            return encoding == null
                    ? StandardCharsets.UTF_8
                    : Charset.forName(encoding);
        } catch (Exception exception) {
            return StandardCharsets.UTF_8;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        if (ip == null || ip.isBlank()) {
            return "UNKNOWN";
        }

        return ip.length() > 64 ? ip.substring(0, 64) : ip;
    }

    /**
     * Captures a bounded copy of the response while writing
     * the original response directly to the client.
     */
    private static class BoundedResponseWrapper
            extends HttpServletResponseWrapper {

        private final ByteArrayOutputStream captured =
                new ByteArrayOutputStream();

        private ServletOutputStream outputStream;
        private PrintWriter writer;
        private boolean streamObtained;
        private boolean writerObtained;

        BoundedResponseWrapper(HttpServletResponse response, int limit) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (writerObtained) {
                throw new IllegalStateException(
                        "getWriter() has already been called."
                );
            }

            streamObtained = true;

            if (outputStream == null) {
                ServletOutputStream original =
                        getResponse().getOutputStream();

                outputStream = new ServletOutputStream() {

                    @Override
                    public void write(int value) throws IOException {
                        original.write(value);

                        if (captured.size() < MAX_BODY_BYTES) {
                            captured.write(value);
                        }
                    }

                    @Override
                    public void write(byte[] bytes, int offset, int length)
                            throws IOException {

                        original.write(bytes, offset, length);

                        int remaining =
                                MAX_BODY_BYTES - captured.size();

                        if (remaining > 0) {
                            captured.write(
                                    bytes,
                                    offset,
                                    Math.min(length, remaining)
                            );
                        }
                    }

                    @Override
                    public boolean isReady() {
                        return original.isReady();
                    }

                    @Override
                    public void setWriteListener(
                            WriteListener listener
                    ) {
                        original.setWriteListener(listener);
                    }

                    @Override
                    public void flush() throws IOException {
                        original.flush();
                    }

                    @Override
                    public void close() throws IOException {
                        original.close();
                    }
                };
            }

            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (streamObtained) {
                throw new IllegalStateException(
                        "getOutputStream() has already been called."
                );
            }

            writerObtained = true;

            if (writer == null) {
                Charset charset = resolveEncoding();

                writer = new PrintWriter(
                        new OutputStreamWriter(
                                getOutputStreamForWriter(),
                                charset
                        ),
                        false
                );
            }

            return writer;
        }

        private ServletOutputStream getOutputStreamForWriter()
                throws IOException {

            if (outputStream == null) {
                // Writer mode writes through the same bounded stream.
                writerObtained = false;
                ServletOutputStream stream = getOutputStream();
                writerObtained = true;
                return stream;
            }

            return outputStream;
        }

        private Charset resolveEncoding() {
            String encoding = getCharacterEncoding();

            try {
                return encoding == null
                        ? StandardCharsets.UTF_8
                        : Charset.forName(encoding);
            } catch (Exception exception) {
                return StandardCharsets.UTF_8;
            }
        }

        byte[] getCapturedBody() {
            if (writer != null) {
                writer.flush();
            }

            return captured.toByteArray();
        }
    }
}
