package com.example.demonet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DemoNetApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void categorySettingsTableExists() {
        var rows = jdbcTemplate.queryForList("SELECT * FROM category_settings");
        assertThat(rows).isNotEmpty();
        assertThat(rows).allMatch(row -> row.get("type") != null);
    }

    @Test
    void itemsTableExists() {
        var count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items", Long.class);
        assertThat(count).isNotNull();
    }
}