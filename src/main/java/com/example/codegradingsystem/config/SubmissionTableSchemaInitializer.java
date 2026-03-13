package com.example.codegradingsystem.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SubmissionTableSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public SubmissionTableSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureLargeTextColumns() {
        if (!tableExists("code_submissions")) {
            return;
        }

        alterColumnIfNeeded("source_code", "LONGTEXT NOT NULL");
        alterColumnIfNeeded("input_data", "LONGTEXT NULL");
        alterColumnIfNeeded("output", "LONGTEXT NULL");
        alterColumnIfNeeded("error_message", "LONGTEXT NULL");
    }

    private void alterColumnIfNeeded(String columnName, String definition) {
        String dataType = jdbcTemplate.query(
                "SELECT DATA_TYPE FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'code_submissions' AND COLUMN_NAME = ?",
                rs -> rs.next() ? rs.getString(1) : null,
                columnName
        );

        if (!"longtext".equalsIgnoreCase(dataType)) {
            jdbcTemplate.execute("ALTER TABLE code_submissions MODIFY COLUMN " + columnName + " " + definition);
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }
}
