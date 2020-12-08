package uk.gov.hmcts.cmc.domain.models.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.Collections;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

@ExtendWith(MockitoExtension.class)
class DefendantTimelineTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Comment"})
    void shouldPassValidationForValidDefendantTimeline(String input) {
        DefendantTimeline timeline = new DefendantTimeline(
            singletonList(TimelineEvent.builder().eventDate("Last Year").description("description").build()),
            input
        );

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    void shouldPassValidationForMaxAllowedEvents() {
        DefendantTimeline timeline = new DefendantTimeline(asList(new TimelineEvent[20]), "comments");

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    void shouldFailValidationForEventLimitExceeds() {
        DefendantTimeline timeline = new DefendantTimeline(asList(new TimelineEvent[1001]), "comments");

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(1)
            .contains("events : size must be between 0 and 1000");
    }

    @Test
    void shouldPassValidationForNoEventInTimeline() {
        DefendantTimeline timeline = new DefendantTimeline(Collections.emptyList(), "comments");

        Set<String> response = validate(timeline);

        assertThat(response)
            .hasSize(0);
    }

    @Test
    void shouldPFailValidationForTooLongComment() {
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
