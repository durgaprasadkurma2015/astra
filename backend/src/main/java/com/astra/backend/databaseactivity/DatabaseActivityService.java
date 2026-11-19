
package com.astra.backend.databaseactivity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatabaseActivityService {

    private static final Logger log =
            LoggerFactory.getLogger(DatabaseActivityService.class);

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public DatabaseActivityService(
            JdbcTemplate jdbc,
            ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getStats() {

        return jdbc.queryForMap(
                """
                SELECT
                    COUNT(*) AS totalChanges,
                    COALESCE(SUM(operation = 'INSERT'), 0) AS inserts,
                    COALESCE(SUM(operation = 'UPDATE'), 0) AS updates,
                    COALESCE(SUM(operation = 'DELETE'), 0) AS deletes,
                    COALESCE(SUM(operation = 'SELECT'), 0) AS selects
                FROM database_change_logs
                """
        );
    }

    public Map<String, Object> getLogs(
            int page,
            int size,
            String table,
            String operation,
            String range,
            String search) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = safePage * safeSize;

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (table != null && !table.isBlank()
                && !"ALL".equalsIgnoreCase(table)) {
            where.append(" AND table_name = ? ");
            params.add(table.trim());
        }

        if (operation != null && !operation.isBlank()
                && !"ALL".equalsIgnoreCase(operation)) {

            String normalized = operation.trim().toUpperCase();

            if (!List.of("INSERT", "UPDATE", "DELETE", "SELECT")
                    .contains(normalized)) {
                throw new IllegalArgumentException(
                        "Invalid operation filter"
                );
            }

            where.append(" AND operation = ? ");
            params.add(normalized);
        }

        LocalDateTime from = rangeStart(range);

        if (from != null) {
            where.append(" AND changed_at >= ? ");
            params.add(Timestamp.valueOf(from));
        }

        if (search != null && !search.isBlank()) {
            String value = "%" + search.trim() + "%";

            where.append("""
                    AND (
                        table_name LIKE ?
                        OR CAST(record_id AS CHAR) LIKE ?
                        OR CAST(old_data AS CHAR) LIKE ?
                        OR CAST(new_data AS CHAR) LIKE ?
                        OR database_user LIKE ?
                    )
                    """);

            params.add(value);
            params.add(value);
            params.add(value);
            params.add(value);
            params.add(value);
        }

        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM database_change_logs " + where,
                Long.class,
                params.toArray()
        );

        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(safeSize);
        pageParams.add(offset);

        List<Map<String, Object>> rows = jdbc.queryForList(
                """
                SELECT
                    id,
                    table_name,
                    operation,
                    record_id,
                    old_data,
                    new_data,
                    changed_columns,
                    database_user,
                    connection_id,
                    changed_at
                FROM database_change_logs
                """
                        + where
                        + """
                        ORDER BY changed_at DESC, id DESC
                        LIMIT ? OFFSET ?
                        """,
                pageParams.toArray()
        );

        List<Map<String, Object>> content = rows.stream()
                .map(this::toResponse)
                .toList();

        long totalElements = total == null ? 0 : total;
        int totalPages = (int) Math.ceil(
                (double) totalElements / safeSize
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("page", safePage);
        response.put("size", safeSize);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);

        return response;
    }

    public List<String> getTables() {
        return jdbc.queryForList(
                """
                SELECT DISTINCT table_name
                FROM database_change_logs
                ORDER BY table_name
                """,
                String.class
        );
    }

    /*
     * Called asynchronously by DatabaseSelectStatementInspector.
     * REQUIRES_NEW ensures the audit insert does not join a read-only
     * transaction from the original SELECT query.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            readOnly = false
    )
    public void recordSelect(String tableName, String sql) {

        try {
            String queryId = UUID.randomUUID().toString();

            String recordId = objectMapper.writeValueAsString(
                    Map.of("queryId", queryId)
            );

            String newData = objectMapper.writeValueAsString(
                    Map.of("sql", sanitizeSql(sql))
            );

            String changedColumns = objectMapper.writeValueAsString(
                    List.of("SQL")
            );

            jdbc.update(
                    """
                    INSERT INTO database_change_logs (
                        table_schema,
                        table_name,
                        operation,
                        record_id,
                        old_data,
                        new_data,
                        changed_columns,
                        database_user,
                        connection_id,
                        changed_at
                    )
                    VALUES (
                        DATABASE(),
                        ?,
                        'SELECT',
                        CAST(? AS JSON),
                        NULL,
                        CAST(? AS JSON),
                        CAST(? AS JSON),
                        USER(),
                        CONNECTION_ID(),
                        CURRENT_TIMESTAMP(6)
                    )
                    """,
                    tableName,
                    recordId,
                    newData,
                    changedColumns
            );

        } catch (Exception ex) {
            log.warn(
                    "Unable to record SELECT activity for table {}: {}",
                    tableName,
                    ex.getMessage()
            );
        }
    }

    private String sanitizeSql(String sql) {

        if (sql == null) {
            return "";
        }

        // Redact quoted SQL literal values.
        String sanitized = sql.replaceAll(
                "'(?:''|[^'])*'",
                "'[REDACTED]'"
        );

        // Prevent excessively large audit records.
        if (sanitized.length() > 10000) {
            sanitized = sanitized.substring(0, 10000)
                    + " ...[TRUNCATED]";
        }

        return sanitized;
    }

    private Map<String, Object> toResponse(
            Map<String, Object> row) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("id", row.get("id"));
        response.put("tableName", row.get("table_name"));
        response.put("operation", row.get("operation"));

        response.put("recordId", parseJson(row.get("record_id")));
        response.put("oldData", parseJson(row.get("old_data")));
        response.put("newData", parseJson(row.get("new_data")));

        response.put(
                "changedColumns",
                parseChangedColumns(row.get("changed_columns"))
        );

        response.put("databaseUser", row.get("database_user"));
        response.put("connectionId", row.get("connection_id"));
        response.put("changedAt", row.get("changed_at"));

        return response;
    }

    private Object parseJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.readValue(
                    value.toString(),
                    new TypeReference<Object>() {}
            );
        } catch (Exception ex) {
            return value.toString();
        }
    }

    private List<String> parseChangedColumns(Object value) {
        if (value == null) {
            return List.of();
        }

        try {
            List<Object> values = objectMapper.readValue(
                    value.toString(),
                    new TypeReference<List<Object>>() {}
            );

            return values.stream()
                    .filter(item -> item != null)
                    .map(Object::toString)
                    .toList();

        } catch (Exception ex) {
            return List.of();
        }
    }

    private LocalDateTime rangeStart(String range) {

        if (range == null || range.isBlank()
                || "ALL".equalsIgnoreCase(range)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        return switch (range.toUpperCase()) {
            case "1H" -> now.minusHours(1);
            case "24H" -> now.minusHours(24);
            case "7D" -> now.minusDays(7);
            case "30D" -> now.minusDays(30);
            case "90D" -> now.minusDays(90);
            default -> throw new IllegalArgumentException(
                    "Invalid date range"
            );
        };
    }
}
