
package com.astra.backend.databaseactivity;

import java.io.Serial;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSelectStatementInspector
        implements StatementInspector {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "(?i)\\b(?:from|join)\\s+((?:`?[\\w$]+`?\\.)?`?[\\w$]+`?)"
    );

    private final DatabaseActivityService service;

    public DatabaseSelectStatementInspector(
            DatabaseActivityService service) {
        this.service = service;
    }

    @Override
    public String inspect(String sql) {

        if (sql == null || sql.isBlank()) {
            return sql;
        }

        String normalized = sql.stripLeading()
                .toLowerCase(Locale.ROOT);

        if (!normalized.startsWith("select")
                && !normalized.startsWith("with")) {
            return sql;
        }

        String tableName = findTableName(sql);

        if (isInternalTable(tableName)) {
            return sql;
        }

        // Run outside the original SELECT transaction.
        CompletableFuture.runAsync(() -> {
            try {
                service.recordSelect(tableName, sql);
            } catch (Exception ex) {
                // Do not interrupt the original SELECT query.
            }
        });

        // Never modify the original SQL.
        return sql;
    }

    private String findTableName(String sql) {

        Matcher matcher = TABLE_PATTERN.matcher(sql);

        while (matcher.find()) {

            String table = matcher.group(1)
                    .replace("`", "");

            if (table.contains(".")) {
                table = table.substring(
                        table.lastIndexOf('.') + 1
                );
            }

            if (!isInternalTable(table)) {
                return table;
            }
        }

        return "UNKNOWN";
    }

    private boolean isInternalTable(String tableName) {

        if (tableName == null) {
            return true;
        }

        String table = tableName.toLowerCase(Locale.ROOT);

        return table.equals("database_change_logs")
                || table.equals("flyway_schema_history")
                || table.equals("information_schema")
                || table.equals("performance_schema")
                || table.equals("mysql")
                || table.equals("sys");
    }
}
