package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class TimelineTest {

    @Test
    public void shouldPassValidationForValidTimeline() {
        TimelineEvent timelineEvent = TimelineEvent.builder().eventDate("Last Year").description("description").build();
        Timeline timeline = new Timeline(Collections.singletonList(timelineEvent));

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldPassValidationForMaxAllowedEvents() {
        Timeline timeline = new Timeline(asList(new TimelineEvent[1000]));

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldFailValidationForEventLimitExceeds() {
        Timeline timeline = new Timeline(asList(new TimelineEvent[1001]));

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(1)
            .contains("events : size must be between 1 and 1000");
    }

    @Test
    public void shouldFailValidationForNoEventInTimeline() {
        Timeline timeline = new Timeline(Collections.emptyList());

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(1)
            .contains("events : size must be between 1 and 1000");
    }

}
