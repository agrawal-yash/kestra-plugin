package io.fss.plugin.imap;

import io.kestra.core.junit.annotations.KestraTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simplified runner test for the IMAP plugin.
 * Full integration tests would require actual IMAP server connection.
 */
@KestraTest
class FetchRunnerTest {

    @Test
    void pluginLoadsCorrectly() {
        // Simple test to ensure the plugin class loads without errors
        Fetch fetch = Fetch.builder()
            .host("imap.gmail.com")
            .username("test@example.com")
            .password("test-password")
            .build();
        
        assertTrue(fetch.getHost().equals("imap.gmail.com"));
        assertTrue(fetch.getUsername().equals("test@example.com"));
        assertTrue(fetch.getPassword().equals("test-password"));
    }
}