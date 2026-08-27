package com.cloudmeal.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationYamlTest {
    @Test
    void applicationYamlMustBeValid() {
        InputStream resource = getClass().getClassLoader().getResourceAsStream("application.yml");
        assertNotNull(resource);
        assertDoesNotThrow(() -> new Yaml().load(resource));
    }
}
