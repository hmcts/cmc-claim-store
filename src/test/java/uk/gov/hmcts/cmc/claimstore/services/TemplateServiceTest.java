package uk.gov.hmcts.cmc.claimstore.services;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.exceptions.TemplateException;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TemplateServiceTest {

    @Mock
    private PebbleEngine mockPebble;
    private PebbleEngine realPebble = new PebbleConfiguration().pebbleEngine();

    private TemplateService service;

    @Test
    public void shouldCorrectlyProcessTemplateWithVariables() {
        service = new TemplateService(realPebble);
        Map<String, Object> variables = singletonMap("key", "Hello, World!");

        String processed = service.evaluate("{{ key }}", variables);

        assertThat(processed).isEqualTo("Hello, World!");
    }

    @Test(expected = TemplateException.class)
    public void shouldThrowTemplateExceptionWhenPebbleExceptionIsThrown() throws Exception {
        when(mockPebble.getTemplate(anyString())).thenThrow(mock(PebbleException.class));
        service = new TemplateService(mockPebble);

        service.evaluate("content doesn't matter", emptyMap());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullTemplateToMapEvaluate() {
        service = new TemplateService(mockPebble);

        service.evaluate(null, emptyMap());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullValuesToMapEvaluate() {
        service = new TemplateService(mockPebble);

        service.evaluate("content doesn't matter",null);
    }

}
