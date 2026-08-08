package com.operation.survival.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PlayerSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public PlayerSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'players'",
                Integer.class);

        if (tableExists == null || tableExists == 0) {
            return;
        }

        Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'players' AND column_name = 'save_version'",
                Integer.class);

        if (columnExists == null || columnExists == 0) {
            jdbcTemplate.execute("ALTER TABLE players ADD COLUMN save_version INT NOT NULL DEFAULT 2");
        } else {
            jdbcTemplate.execute("ALTER TABLE players MODIFY COLUMN save_version INT NOT NULL DEFAULT 2");
            jdbcTemplate.execute("UPDATE players SET save_version = 2 WHERE save_version IS NULL");
        }
    }
}
