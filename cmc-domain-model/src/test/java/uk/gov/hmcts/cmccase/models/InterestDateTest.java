package uk.gov.hmcts.cmccase.models;

import org.junit.Test;
import uk.gov.hmcts.cmccase.models.sampledata.SampleInterestDate;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmccase.utils.BeanValidator.validate;

public class InterestDateTest {

    @Test
    public void shouldFailWhenInterestDateAttributesAreNull() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
            .withType(null)
            .withDate(null)
            .withReason(null)
            .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors)
            .hasSize(2)
            .contains(
                "reason : may not be empty",
                "type : may not be null"
            );
    }

    @Test
    public void shouldBeValidForCustomInterestDateInThePast() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
            .withType(InterestDate.InterestDateType.CUSTOM)
            .withDate(LocalDate.of(2015, 2, 5))
            .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldValidForSubmissionTypeAndDateInThePast() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
            .withType(InterestDate.InterestDateType.SUBMISSION)
            .withDate(LocalDate.of(2015, 2, 5))
            .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnValidationMessageForCustomTypeAndDateInFuture() {
        //given
        LocalDate todayPlusOne = LocalDate.now().plusDays(1);
        InterestDate interestDate = SampleInterestDate.builder()
            .withType(InterestDate.InterestDateType.CUSTOM)
            .withDate(todayPlusOne)
            .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("date : is in the future");
    }

    @Test
    public void shouldReturnValidationMessageForCustomTypeAndBlankReason() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
            .withType(InterestDate.InterestDateType.CUSTOM)
            .withReason("")
            .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("reason : may not be empty");
    }

    @Test
    public void shouldBeValidForSubmissionTypeAndNullReason() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
            .withType(InterestDate.InterestDateType.SUBMISSION)
            .withReason(null)
            .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors).isEmpty();
    }

}
