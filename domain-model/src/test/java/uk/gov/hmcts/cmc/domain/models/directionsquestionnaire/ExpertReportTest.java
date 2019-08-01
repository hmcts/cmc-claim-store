package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import org.junit.Test;

import java.util.Set;

import static java.time.LocalDate.now;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class ExpertReportTest {

    @Test
    public void shouldBeSuccessfulValidationForExpertReport() {
        ExpertReport expertReport = ExpertReport
            .builder()
            .expertName("Some name")
            .expertReportDate(now())
            .build();

        Set<String> response = validate(expertReport);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldBeValidationMessagesWhenMissingReportDate() {
        ExpertReport expertReport = ExpertReport
            .builder()
            .expertName("Some name")
            .build();

        Set<String> response = validate(expertReport);

        assertThat(response)
            .hasSize(1)
            .contains("expertReportDate : may not be null");
    }

    @Test
    public void shouldBeValidationMessagesWhenMissingReportName() {
        ExpertReport expertReport = ExpertReport
            .builder()
            .expertReportDate(now())
            .build();

        Set<String> response = validate(expertReport);

        assertThat(response)
            .hasSize(1)
            .contains("expertName : may not be null");
    }

    @Test
    public void shouldBeValidationMessagesWhenExpertReportNameIsLongerThanMax() {
        ExpertReport expertReport = ExpertReport
            .builder()
            .expertName(randomAlphabetic(101))
            .expertReportDate(now())
            .build();

        Set<String> response = validate(expertReport);

        assertThat(response)
            .hasSize(1)
            .contains("expertName : size must be between 0 and 100");
    }
}
