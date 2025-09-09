package io.fss.plugin.imap;

import com.google.common.collect.ImmutableMap;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import io.kestra.core.runners.RunContextFactory;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test will only test the main task, this allow you to send any input
 * parameters to your task and test the returning behaviour easily.
 */
@MicronautTest
class FetchTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run() throws Exception {
        runContextFactory.of(ImmutableMap.of(
            "host", "imap.gmail.com",
            "username", "test@example.com",
            "password", "test-password"
        ));

        Fetch task = Fetch.builder()
            .host("{{ host }}")
            .username("{{ username }}")
            .password("{{ password }}")
            .maxEmails(1)
            .build();

        // Test that the task properties are set correctly
        assertEquals("{{ host }}", task.getHost());
        assertEquals("{{ username }}", task.getUsername());
        assertEquals("{{ password }}", task.getPassword());
        assertEquals(Integer.valueOf(1), task.getMaxEmails());
        
        // Note: We don't test actual IMAP connection as it would require a real server
        // This test verifies the task configuration and basic setup
        assertNotNull(task);
        assertTrue(task.getUseSsl()); // Default should be true
        assertEquals("INBOX", task.getFolder()); // Default folder
    }
}