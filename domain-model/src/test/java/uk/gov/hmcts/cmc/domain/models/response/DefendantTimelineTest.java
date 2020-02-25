package uk.gov.hmcts.cmc.domain.models.response;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.Collections;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class DefendantTimelineTest {

    @Test
    public void shouldPassValidationForValidDefendantTimeline() {
        DefendantTimeline timeline = new DefendantTimeline(
            singletonList(TimelineEvent.builder().eventDate("Last Year").description("description").build()),
            "comments"
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
        DefendantTimeline timeline = new DefendantTimeline(asList(new TimelineEvent[1001]), "comments");

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(1)
            .contains("events : size must be between 0 and 1000");
    }

    @Test
    public void shouldPassValidationForNoEventInTimeline() {
        DefendantTimeline timeline = new DefendantTimeline(Collections.emptyList(), "comments");

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldPassValidationForNullComment() {
        DefendantTimeline timeline = new DefendantTimeline(
            singletonList(TimelineEvent.builder().eventDate("Last Year").description("description").build()),
            null
        );

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldPassValidationForEmptyComment() {
        DefendantTimeline timeline = new DefendantTimeline(
            singletonList(TimelineEvent.builder().eventDate("Last Year").description("description").build()),
            ""
        );

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    public void shouldPFailValidationForTooLongComment() {
        DefendantTimeline timeline = new DefendantTimeline(
            singletonList(TimelineEvent.builder().eventDate("Last Year")
                .description("description").build()), repeat("a", 99001)
        );

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(1)
            .contains("comment : size must be between 0 and 99000");
    }
}
