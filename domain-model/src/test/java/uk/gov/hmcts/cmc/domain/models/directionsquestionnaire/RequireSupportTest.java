package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import org.junit.Test;

import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class RequireSupportTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectRequireSupport() {
        RequireSupport requireSupport = RequireSupport
            .builder()
            .languageInterpreter("My language")
            .signLanguageInterpreter("Sign language")
            .otherSupport("Some other support")
            .build();

        Set<String> response = validate(requireSupport);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldBeValidationMessagesForLanguageInterpreterThanMax() {
        RequireSupport requireSupport = RequireSupport
            .builder()
            .languageInterpreter(randomAlphabetic(101))
            .signLanguageInterpreter("Sign language")
            .otherSupport("Some other support")
            .build();

        //when
        Set<String> messages = validate(requireSupport);

        //then
        assertThat(messages)
            .hasSize(1)
            .contains("languageInterpreter : size must be between 0 and 100");
    }

    @Test
    public void shouldBeValidationMessagesForSignLanguageInterpreterThanMax() {
        RequireSupport requireSupport = RequireSupport
            .builder()
            .signLanguageInterpreter(randomAlphabetic(101))
            .otherSupport("Some other support")
            .build();

        //when
        Set<String> messages = validate(requireSupport);

        //then
        assertThat(messages)
            .hasSize(1)
            .contains("signLanguageInterpreter : size must be between 0 and 100");
    }

    @Test
    public void shouldBeValidationMessagesForOtherSupportThanMax() {
        RequireSupport requireSupport = RequireSupport
            .builder()
            .otherSupport(randomAlphabetic(99001))
            .build();

        //when
        Set<String> messages = validate(requireSupport);

        //then
        assertThat(messages)
            .hasSize(1)
            .contains("otherSupport : size must be between 0 and 99000");
    }
}
