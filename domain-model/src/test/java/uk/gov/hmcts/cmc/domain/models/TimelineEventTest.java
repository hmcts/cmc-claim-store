package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class TimelineEventTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectTimelineEvent() {
        TimelineEvent timelineEvent = new TimelineEvent("Last Year", "description");

        Set<String> response = validate(timelineEvent);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldFailValidationForNullEventDate() {
        TimelineEvent timelineEvent = new TimelineEvent(null, "description");

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("date : may not be empty");
    }

    @Test
    public void shouldFailValidationForEmptyEventDate() {
        TimelineEvent timelineEvent = new TimelineEvent("", "description");

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("date : may not be empty");
    }

    @Test
    public void shouldFailValidationForTooLongEventDate() {
        TimelineEvent timelineEvent = new TimelineEvent(repeat("a", 21), "description");

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("date : size must be between 0 and 20");
    }

    @Test
    public void shouldFailValidationForNullDescription() {
        TimelineEvent timelineEvent = new TimelineEvent("Last Year", null);

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("description : may not be empty");
    }

    @Test
    public void shouldFailValidationForEmptyDescription() {
        TimelineEvent timelineEvent = new TimelineEvent("Last Year", "");

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("description : may not be empty");
    }

    @Test
    public void shouldFailValidationForTooLongDescription() {
        TimelineEvent timelineEvent = new TimelineEvent("Last Year", repeat("a", 99001));

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("description : size must be between 0 and 99000");
    }
}
