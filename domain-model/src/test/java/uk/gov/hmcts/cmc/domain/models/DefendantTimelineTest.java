package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class DefendantTimelineTest {

    @Test
    public void shouldPassValidationForValidDefendantTimeline() {
        DefendantTimeline timeline = new DefendantTimeline(
            asList(new TimelineEvent("Last Year", "description"))
            , "comments"
        );

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldPassValidationForMaxAllowedEvents() {
        DefendantTimeline timeline = new DefendantTimeline(asList(new TimelineEvent[20]), "comments");

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldFailValidationForEventLimitExceeds() {
        DefendantTimeline timeline = new DefendantTimeline(asList(new TimelineEvent[21]), "comments");

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(1)
            .contains("events : size must be between 0 and 20");
    }

}
