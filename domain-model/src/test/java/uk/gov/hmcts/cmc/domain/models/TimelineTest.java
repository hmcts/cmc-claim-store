package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class TimelineTest {

    @Test
    public void shouldPassValidationForValidTimeline() {
        Timeline timeline = new Timeline(asList(new TimelineEvent("Last Year", "description")));

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldPassValidationForMaxAllowedEvents() {
        Timeline timeline = new Timeline(asList(new TimelineEvent[20]));

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldFailValidationForEventLimitExceeds() {
        Timeline timeline = new Timeline(asList(new TimelineEvent[21]));

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(1)
            .contains("events : size must be between 0 and 20");
    }

}
