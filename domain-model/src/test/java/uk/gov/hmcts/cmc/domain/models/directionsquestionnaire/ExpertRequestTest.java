package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import org.junit.Test;

import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class ExpertRequestTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectExpertRequest() {
        ExpertRequest expertRequest = ExpertRequest
            .builder()
            .expertEvidenceToExamine("My evidence")
            .reasonForExpertAdvice("My reason")
            .build();

        Set<String> response = validate(expertRequest);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldBeValidationMessagesWhenExpertEvidenceToExamineIsLongerThanMax() {

        ExpertRequest expertRequest = ExpertRequest
            .builder()
            .expertEvidenceToExamine(randomAlphabetic(1001))
            .reasonForExpertAdvice("My reason")
            .build();

        Set<String> messages = validate(expertRequest);

        assertThat(messages)
            .hasSize(1)
            .contains("expertEvidenceToExamine : size must be between 0 and 1000");
    }

    @Test
    public void shouldBeValidationMessagesWhenReasonForExpertAdviceIsLongerThanMax() {

        ExpertRequest expertRequest = ExpertRequest
            .builder()
            .expertEvidenceToExamine("My evidence")
            .reasonForExpertAdvice(randomAlphabetic(99001))
            .build();

        Set<String> messages = validate(expertRequest);

        assertThat(messages)
            .hasSize(1)
            .contains("reasonForExpertAdvice : size must be between 0 and 99000");
    }

    @Test
    public void shouldBeValidationMessagesWhenExpertEvidenceToExamineIsNull() {

        ExpertRequest expertRequest = ExpertRequest
            .builder()
            .reasonForExpertAdvice("My reason")
            .build();

        Set<String> messages = validate(expertRequest);

        assertThat(messages)
            .hasSize(1)
            .contains("expertEvidenceToExamine : may not be null");
    }

    @Test
    public void shouldBeValidationMessagesWhenReasonForExpertAdviceIsNull() {

        ExpertRequest expertRequest = ExpertRequest
            .builder()
            .expertEvidenceToExamine("My evidence")
            .build();

        Set<String> messages = validate(expertRequest);

        assertThat(messages)
            .hasSize(1)
            .contains("reasonForExpertAdvice : may not be null");
    }
}
