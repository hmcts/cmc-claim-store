package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class TimelineEventTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectTimelineEvent() {
        TimelineEvent timelineEvent = TimelineEvent.builder().eventDate("Last Year").description("description").build();

        Set<String> response = validate(timelineEvent);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldFailValidationForNullEventDate() {
        TimelineEvent timelineEvent = TimelineEvent.builder().description("description").build();

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("date : may not be empty");
    }

    @Test
    public void shouldFailValidationForEmptyEventDate() {
        TimelineEvent timelineEvent = TimelineEvent.builder().eventDate("").description("description").build();

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("date : may not be empty");
    }

    @Test
    public void shouldFailValidationForTooLongEventDate() {
        TimelineEvent timelineEvent = TimelineEvent.builder()
            .eventDate(repeat("a", 21)).description("description").build();

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("date : size must be between 0 and 20");
    }

    @Test
    public void shouldFailValidationForNullDescription() {
        TimelineEvent timelineEvent = TimelineEvent.builder().eventDate("Last Year").description(null).build();

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("description : may not be empty");
    }

    @Test
    public void shouldFailValidationForEmptyDescription() {
        TimelineEvent timelineEvent = TimelineEvent.builder().eventDate("Last Year").description("").build();

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("description : may not be empty");
    }

    @Test
    public void shouldFailValidationForTooLongDescription() {
        TimelineEvent timelineEvent = TimelineEvent.builder().eventDate("Last Year")
            .description(repeat("a", 99001)).build();

        Set<String> response = validate(timelineEvent);

        assertThat(response)
            .hasSize(1)
            .contains("description : size must be between 0 and 99000");
    }
}
