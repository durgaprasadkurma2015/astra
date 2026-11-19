
package com.astra.backend.monitoring;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class ApiLogSanitizer {

    private static final int MAX_FIELD_LENGTH = 8000;
    private static final int MAX_STRING_LENGTH = 2000;

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "authorization",
            "password",
            "passwd",
            "secret",
            "token",
            "accesstoken",
            "refreshtoken",
            "cookie",
            "set-cookie",
            "session",
            "sessionid",
            "otp",
            "cvv",
            "cvc",
            "cardnumber",
            "card_number",
            "cardholder",
            "accountnumber",
            "account_number",
            "apikey",
            "privatekey"
    );

    private static final Pattern QUERY_SECRET = Pattern.compile(
            "(?i)(password|token|secret|key|otp|session|code)=([^&]*)"
    );

    private ApiLogSanitizer() {
    }

    public static String sanitizeJson(
            JsonNode value,
            ObjectMapper objectMapper
    ) {

        if (value == null || value.isNull()) {
            return null;
        }

        try {
            JsonNode sanitized = sanitizeNode(value);

            String result = objectMapper.writeValueAsString(sanitized);

            if (result.length() > MAX_FIELD_LENGTH) {
                return result.substring(0, MAX_FIELD_LENGTH)
                        + "...[TRUNCATED]";
            }

            return result;

        } catch (Exception exception) {
            return "[UNAVAILABLE]";
        }
    }

    public static String sanitizeText(String value) {

        if (value == null) {
            return null;
        }

        String sanitized = QUERY_SECRET.matcher(value)
                .replaceAll("$1=[REDACTED]");

        if (sanitized.length() > MAX_FIELD_LENGTH) {
            return sanitized.substring(0, MAX_FIELD_LENGTH)
                    + "...[TRUNCATED]";
        }

        return sanitized;
    }

    public static String sanitizeEndpoint(String endpoint) {

        if (endpoint == null) {
            return "/";
        }

        return sanitizeText(endpoint);
    }

    private static JsonNode sanitizeNode(JsonNode node) {

        if (node == null || node.isNull()) {
            return node;
        }

        if (node.isObject()) {

            ObjectNode sanitized = ((ObjectNode) node).deepCopy();

            for (Map.Entry<String, JsonNode> field : node.properties()) {

                if (isSensitiveKey(field.getKey())) {

                    sanitized.put(field.getKey(), "[REDACTED]");

                } else {

                    sanitized.set(
                            field.getKey(),
                            sanitizeNode(field.getValue())
                    );
                }
            }

            return sanitized;
        }

        if (node.isArray()) {

            ArrayNode sanitized =
                    JsonNodeFactory.instance.arrayNode();

            for (JsonNode item : node) {
                sanitized.add(sanitizeNode(item));
            }

            return sanitized;
        }

        if (node.isString()) {

            String value = node.stringValue();

            if (value.length() > MAX_STRING_LENGTH) {
                value = value.substring(0, MAX_STRING_LENGTH)
                        + "...[TRUNCATED]";
            }

            return JsonNodeFactory.instance.stringNode(value);
        }

        return node.deepCopy();
    }

    private static boolean isSensitiveKey(String key) {

        String normalized = key
                .replace("-", "")
                .replace("_", "")
                .replace(".", "")
                .toLowerCase(Locale.ROOT);

        if (SENSITIVE_KEYS.contains(key.toLowerCase(Locale.ROOT))) {
            return true;
        }

        for (String sensitive : SENSITIVE_KEYS) {

            String normalizedSensitive = sensitive
                    .replace("-", "")
                    .replace("_", "")
                    .toLowerCase(Locale.ROOT);

            if (normalized.equals(normalizedSensitive)) {
                return true;
            }
        }

        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("authorization")
                || normalized.contains("cookie");
    }
}
