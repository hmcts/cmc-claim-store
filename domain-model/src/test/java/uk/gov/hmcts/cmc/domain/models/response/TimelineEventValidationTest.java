package uk.gov.hmcts.cmc.domain.models.response;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleTimelineEvent;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.BeanValidator.validate;

public class TimelineEventValidationTest {

    @Test
    public void passesForValidSample() {
        //given
        TimelineEvent timelineEvent = SampleTimelineEvent.validDefaults();
        //when
        Set<String> response = validate(timelineEvent);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void failsWhenDescriptionEmpty() {
        //given
        TimelineEvent timelineEvent = SampleTimelineEvent.builder()
            .withDescription(null)
            .build();
        //when
        Set<String> errors = validate(timelineEvent);
        //then
        assertThat(errors)
            .containsExactly("description : may not be empty");
    }

    @Test
    public void failsWhenDescriptionTooLong() {
        //given
        TimelineEvent timelineEvent = SampleTimelineEvent.builder()
            .withDescription(StringUtils.repeat("a", 99001))
            .build();
        //when
        Set<String> errors = validate(timelineEvent);
        //then
        assertThat(errors)
            .containsExactly("description : size must be between 0 and 99000");
    }

    @Test
    public void failsWhenDateTooLong() {
        //given
        TimelineEvent timelineEvent = SampleTimelineEvent.builder()
            .withDate(StringUtils.repeat("a", 26))
            .build();
        //when
        Set<String> errors = validate(timelineEvent);
        //then
        assertThat(errors)
            .containsExactly("date : size must be between 0 and 25");
    }
}
