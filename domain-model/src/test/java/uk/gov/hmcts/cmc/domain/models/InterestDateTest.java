package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType.SUBMISSION;

public class InterestDateTest {

    @Test
    public void withCustomType_shouldBeSuccessfulValidationWhenDateInThePast() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.CUSTOM)
                .withDate(LocalDate.of(2015, 2, 5))
                .withReason("reason")
                .withEndDateType(SUBMISSION)
                .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void withCustomType_shouldBeInvalidWhenDateInTheFuture() {
        //given
        LocalDate todayPlusOne = LocalDate.now().plusDays(1);
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.CUSTOM)
                .withDate(todayPlusOne)
                .withReason("reason")
                .withEndDateType(SUBMISSION)
                .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors)
                .containsExactlyInAnyOrder("date : is in the future");
    }

    @Test
    public void withCustomType_shouldBeInvalidWhenMandatoryFieldsMissing() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.CUSTOM)
                .withDate(null)
                .withReason(null)
                .withEndDateType(null)
                .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors)
                .containsExactlyInAnyOrder(
                        "date : may not be null",
                        "reason : may not be null");
    }

    @Test
    public void withSubmissionType_shouldBeSuccessfulValidation() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.SUBMISSION)
                .withDate(null)
                .withReason(null)
                .withEndDateType(SUBMISSION)
                .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void withSubmissionType_shouldBeInvalidWhenUnexpectedFieldsProvided() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.SUBMISSION)
                .withDate(LocalDate.of(2015, 2, 5))
                .withReason("reason")
                .withEndDateType(SETTLED_OR_JUDGMENT)
                .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors)
                .containsExactlyInAnyOrder(
                        "date : may not be provided when type is 'submission'",
                        "reason : may not be provided when type is 'submission'");
    }

    @Test
    public void withNullType_shouldBeSuccessfulValidation() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(null)
                .withDate(null)
                .withReason(null)
                .withEndDateType(SUBMISSION)
                .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void withNullType_shouldBeInvalidWhenUnexpectedFieldsProvided() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(null)
                .withDate(LocalDate.of(2015, 2, 5))
                .withReason("reason")
                .withEndDateType(SETTLED_OR_JUDGMENT)
                .build();
        //when
        Set<String> errors = validate(interestDate);
        //then
        assertThat(errors)
                .containsExactlyInAnyOrder(
                        "date : may not be provided when type is undefined",
                        "reason : may not be provided when type is undefined");
    }

    @Test
    public void withCustomType_shouldIsCustomReturnTrue() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.CUSTOM)
                .withDate(LocalDate.of(2015, 2, 5))
                .build();

        //then
        assertThat(interestDate.isCustom()).isEqualTo(true);
    }

    @Test
    public void withCustomType_shouldIsEndDateOnSubmissionReturnFalse() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.CUSTOM)
                .withDate(LocalDate.of(2015, 2, 5))
                .build();

        //then
        assertThat(interestDate.isEndDateOnSubmission()).isEqualTo(false);
    }

    @Test
    public void withSubmissionType_shouldIsCustomReturnFalse() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.SUBMISSION)
                .build();

        //then
        assertThat(interestDate.isCustom()).isEqualTo(false);
    }

    @Test
    public void withSubmissionType_shouldIsEndDateOnClaimCompleteReturnTrueWhenEndDateTypeIsSettledOrJudgment() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.SUBMISSION)
                .withEndDateType(InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT)
                .build();

        //then
        assertThat(interestDate.isEndDateOnClaimComplete()).isEqualTo(true);
        assertThat(interestDate.isEndDateOnSubmission()).isEqualTo(false);
    }

    @Test
    public void withSubmissionType_shouldIsEndDateOnClaimCompleteReturnFalseWhenEndDateTypeIsSubmission() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.SUBMISSION)
                .withEndDateType(SUBMISSION)
                .build();

        //then
        assertThat(interestDate.isEndDateOnSubmission()).isEqualTo(true);
        assertThat(interestDate.isEndDateOnClaimComplete()).isEqualTo(false);
    }

    @Test
    public void withSubmissionType_shouldIsEndDateOnClaimCompleteReturnFalseWhenEndDateTypeIsSetNull() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
                .withType(InterestDate.InterestDateType.SUBMISSION)
                .withEndDateType(null)
                .build();

        //then
        assertThat(interestDate.isEndDateOnSubmission()).isEqualTo(false);
        assertThat(interestDate.isEndDateOnClaimComplete()).isEqualTo(true);
    }
}
