
package com.astra.backend.databaseactivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseActivityTriggerInstaller implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(DatabaseActivityTriggerInstaller.class);

    private static final String LOG_TABLE = "database_change_logs";

    private final JdbcTemplate jdbc;

    public DatabaseActivityTriggerInstaller(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        String schema = jdbc.queryForObject(
                "SELECT DATABASE()", String.class);

        if (schema == null || schema.isBlank()) {
            log.warn("Database Activity: no active schema found.");
            return;
        }

        List<String> tables = jdbc.queryForList(
                """
                SELECT TABLE_NAME
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = ?
                  AND TABLE_TYPE = 'BASE TABLE'
                  AND TABLE_NAME NOT IN (
                      'database_change_logs',
                      'flyway_schema_history'
                  )
                ORDER BY TABLE_NAME
                """,
                String.class,
                schema
        );

        for (String table : tables) {
            try {
                installForTable(schema, table);
            } catch (Exception ex) {
                log.error(
                        "Could not install database activity triggers for table {}",
                        table,
                        ex
                );
            }
        }

        log.info(
                "Database Activity trigger installation checked {} tables.",
                tables.size()
        );
    }

    private void installForTable(String schema, String table) {

        List<String> columns = jdbc.queryForList(
                """
                SELECT COLUMN_NAME
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = ?
                  AND TABLE_NAME = ?
                  AND DATA_TYPE NOT IN (
                      'blob', 'tinyblob', 'mediumblob', 'longblob',
                      'binary', 'varbinary', 'geometry',
                      'point', 'linestring', 'polygon',
                      'multipoint', 'multilinestring',
                      'multipolygon', 'geometrycollection'
                  )
                ORDER BY ORDINAL_POSITION
                """,
                String.class,
                schema,
                table
        );

        if (columns.isEmpty()) {
            log.warn("Skipping table {} because it has no auditable columns.", table);
            return;
        }

        List<String> primaryKeys = jdbc.queryForList(
                """
                SELECT COLUMN_NAME
                FROM information_schema.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = ?
                  AND TABLE_NAME = ?
                  AND CONSTRAINT_NAME = 'PRIMARY'
                ORDER BY ORDINAL_POSITION
                """,
                String.class,
                schema,
                table
        );

        if (primaryKeys.isEmpty()) {
            log.warn(
                    "Table {} has no primary key. Its audit record cannot identify a unique row.",
                    table
            );
        }

        String recordId = jsonObjectExpression(
                primaryKeys.isEmpty() ? List.of() : primaryKeys,
                "NEW"
        );

        if (primaryKeys.isEmpty()) {
            recordId = "JSON_OBJECT('warning', 'TABLE_HAS_NO_PRIMARY_KEY')";
        }

        String oldData = jsonObjectExpression(columns, "OLD");
        String newData = jsonObjectExpression(columns, "NEW");

        String changedColumns = changedColumnsExpression(columns);

        createTrigger(
                schema,
                table,
                "INSERT",
                "AFTER INSERT",
                recordId,
                "NULL",
                newData,
                "JSON_ARRAY()"
        );

        createTrigger(
                schema,
                table,
                "UPDATE",
                "AFTER UPDATE",
                jsonObjectExpression(
                        primaryKeys.isEmpty() ? List.of() : primaryKeys,
                        "NEW"
                ),
                oldData,
                newData,
                changedColumns
        );

        createTrigger(
                schema,
                table,
                "DELETE",
                "AFTER DELETE",
                jsonObjectExpression(
                        primaryKeys.isEmpty() ? List.of() : primaryKeys,
                        "OLD"
                ),
                oldData,
                "NULL",
                "JSON_ARRAY()"
        );
    }

    private void createTrigger(
            String schema,
            String table,
            String operation,
            String timing,
            String recordId,
            String oldData,
            String newData,
            String changedColumns) {

        String triggerName = triggerName(table, operation);

        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.TRIGGERS
                WHERE TRIGGER_SCHEMA = ?
                  AND TRIGGER_NAME = ?
                """,
                Integer.class,
                schema,
                triggerName
        );

        if (count != null && count > 0) {
            return;
        }

        String sql = """
                CREATE TRIGGER %s
                %s ON %s
                FOR EACH ROW
                INSERT INTO %s (
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
                    '%s',
                    '%s',
                    %s,
                    %s,
                    %s,
                    %s,
                    USER(),
                    CONNECTION_ID(),
                    CURRENT_TIMESTAMP(6)
                )
                """.formatted(
                quote(triggerName),
                timing,
                qualifiedTable(schema, table),
                qualifiedTable(schema, LOG_TABLE),
                escapeSql(table),
                operation,
                recordId,
                oldData,
                newData,
                changedColumns
        );

        jdbc.execute(sql);

        log.info(
                "Installed {} trigger for table {}",
                operation,
                table
        );
    }

    private String jsonObjectExpression(
            List<String> columns,
            String rowAlias) {

        if (columns.isEmpty()) {
            return "JSON_OBJECT()";
        }

        List<String> pairs = new ArrayList<>();

        for (String column : columns) {
            String key = "'" + escapeSql(column) + "'";

            if (isSensitiveColumn(column)) {
                pairs.add(key);
                pairs.add("'[REDACTED]'");
            } else {
                pairs.add(key);
                pairs.add(rowAlias + "." + quote(column));
            }
        }

        return "JSON_OBJECT(" + String.join(", ", pairs) + ")";
    }

    private String changedColumnsExpression(List<String> columns) {

        List<String> expressions = new ArrayList<>();

        for (String column : columns) {
            expressions.add(
                    "IF(NOT (OLD." + quote(column)
                    + " <=> NEW." + quote(column) + "), '"
                    + escapeSql(column) + "', NULL)"
            );
        }

        return expressions.isEmpty()
                ? "JSON_ARRAY()"
                : "JSON_ARRAY(" + String.join(", ", expressions) + ")";
    }

    private boolean isSensitiveColumn(String column) {
        String name = column.toLowerCase();

        return name.contains("password")
                || name.contains("token")
                || name.contains("secret")
                || name.contains("otp")
                || name.contains("api_key")
                || name.contains("private_key")
                || name.contains("card_number")
                || name.contains("cvv");
    }

    private String triggerName(String table, String operation) {
        return "astra_audit_"
                + shortHash(table)
                + "_"
                + operation.toLowerCase();
    }

    private String shortHash(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash).substring(0, 12);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate trigger name", ex);
        }
    }

    private String qualifiedTable(String schema, String table) {
        return quote(schema) + "." + quote(table);
    }

    private String quote(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private String escapeSql(String value) {
        return value.replace("\\", "\\\\").replace("'", "''");
    }
}
