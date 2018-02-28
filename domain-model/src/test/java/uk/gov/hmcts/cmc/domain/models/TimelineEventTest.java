package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import java.util.Set;

import static java.time.LocalDate.now;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class TimelineEventTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectTimelineEvent() {
        TimelineEvent timelineEvent = new TimelineEvent(now(), "description");

        Set<String> response = validate(timelineEvent);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldFailValidationForNullEventDate() {
        TimelineEvent timelineEvent = new TimelineEvent(null, "description");

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("eventDate : may not be null");
    }

    @Test
    public void shouldFailValidationForFutureEventDate() {
        TimelineEvent timelineEvent = new TimelineEvent(now().plusDays(1), "description");

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("eventDate : is in the future");
    }

    @Test
    public void shouldFailValidationForNullDescription() {
        TimelineEvent timelineEvent = new TimelineEvent(now(), null);

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("description : may not be empty");
    }

    @Test
    public void shouldFailValidationForEmptyDescription() {
        TimelineEvent timelineEvent = new TimelineEvent(now(), "");

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("description : may not be empty");
    }

    @Test
    public void shouldFailValidationForDescriptionTooLong() {
        TimelineEvent timelineEvent = new TimelineEvent(now(), repeat("a", 99001));

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("description : size must be between 0 and 99000");
    }
}
