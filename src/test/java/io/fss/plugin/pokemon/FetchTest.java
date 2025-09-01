package io.fss.plugin.pokemon;

import com.google.common.collect.ImmutableMap;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
        RunContext runContext = runContextFactory.of(ImmutableMap.of("variable", "gengar"));

        Fetch task = Fetch.builder()
            .pokemon("{{ variable }}")
            .build();

        Fetch.Output runOutput = task.run(runContext);
        assertThat(runOutput.getBaseExperience(), is(250L));
        assertThat(runOutput.getHeight(), is(15L));
        assertThat(runOutput.getAbilities().size(), is(1));
        assertThat(runOutput.getMoves().size(), is(123));
    }
}