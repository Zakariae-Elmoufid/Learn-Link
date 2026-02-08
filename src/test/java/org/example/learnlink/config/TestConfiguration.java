package org.example.learnlink.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Test configuration for Spring Boot tests
 * Ensures H2 database is used instead of PostgreSQL
 */
@TestConfiguration
public class TestConfiguration {

    // Additional test beans can be added here if needed
}

