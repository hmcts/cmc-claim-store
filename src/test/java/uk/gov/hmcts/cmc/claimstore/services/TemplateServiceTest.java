package uk.gov.hmcts.cmc.claimstore.services;

import com.mitchellbosecke.pebble.PebbleEngine;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.exceptions.TemplateException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class TemplateServiceTest {

    private final PebbleEngine pebble = new PebbleConfiguration().pebbleEngine();

    private TemplateService service;

    @Test
    public void shouldCorrectlyProcessTemplateWithVariables() {
        service = new TemplateService(pebble);
        Map<String, Object> variables = singletonMap("key", "Hello, World!");

        String processed = service.evaluate("{{ key }}", variables);

        assertThat(processed).isEqualTo("Hello, World!");
    }

    @Test(expected = TemplateException.class)
    public void shouldThrowTemplateExceptionOnEmptyObject() {
        service = new TemplateService(pebble);
        Map<String, Object> variables = new HashMap<>();

        service.evaluate("{{ person }}", variables);
    }

    @Test(expected = TemplateException.class)
    public void shouldThrowTemplateExceptionWhenPebbleExceptionIsThrown() {
        service = new TemplateService(pebble);

        service.evaluate("block {{", emptyMap());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullTemplateToMapEvaluate() {
        service = new TemplateService(pebble);

        service.evaluate(null, emptyMap());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullValuesToMapEvaluate() {
        service = new TemplateService(pebble);

        service.evaluate("content doesn't matter", null);
    }

}
