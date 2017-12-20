package uk.gov.hmcts.cmc.domain.models.response;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTimeLineEvent;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.BeanValidator.validate;

public class TimelineEventTest {

    @Test
    public void shouldBeSuccessfulValidationForTimeLineRow() {
        //given
        TimelineEvent timelineEvent = SampleTimeLineEvent.validDefaults();
        //when
        Set<String> response = validate(timelineEvent);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForNullExplanation() {
        //given
        TimelineEvent timelineEvent = SampleTimeLineEvent.builder()
            .withExplanation(null)
            .build();
        //when
        Set<String> errors = validate(timelineEvent);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("description : may not be empty");
    }

    @Test
    public void shouldBeInvalidForTooLongExplanation() {
        //given
        TimelineEvent timelineEvent = SampleTimeLineEvent.builder()
            .withExplanation(StringUtils.repeat("a", 990000))
            .build();
        //when
        Set<String> errors = validate(timelineEvent);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("description : size must be between 0 and 99000");
    }

    @Test
    public void shouldBeInvalidForTooLongDate() {
        //given
        TimelineEvent timelineEvent = SampleTimeLineEvent.builder()
            .withDate(StringUtils.repeat("a", 100))
            .build();
        //when
        Set<String> errors = validate(timelineEvent);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("date : size must be between 0 and 25");
    }
}
