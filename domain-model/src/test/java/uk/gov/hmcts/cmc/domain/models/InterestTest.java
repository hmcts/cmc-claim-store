package uk.gov.hmcts.cmc.domain.models;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;
import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.BREAKDOWN;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.DIFFERENT;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.NO_INTEREST;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.STANDARD;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestDateType.CUSTOM;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestDateType.SUBMISSION;

public class InterestTest {

    @Test
    public void shouldBeInvalidWhenAllFieldsAreNull() {
        //given
        Interest interest = SampleInterest
                .builder()
                .withType(null)
                .withRate(null)
                .withReason(null)
                .withInterestBreakdown(null)
                .withInterestDate(null)
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
                .containsExactly("type : must not be null");
    }

    @Test
    public void withNoInterestType_shouldBeSuccessful() {
        //given
        Interest interest = SampleInterest
                .builder()
                .withType(NO_INTEREST)
                .withRate(null)
                .withReason(null)
                .withInterestBreakdown(null)
                .withInterestDate(null)
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void withNoInterestType_shouldBeInvalidWhenUnexpectedFieldsProvided() {
        //given
        Interest interest = SampleInterest
                .builder()
                .withType(NO_INTEREST)
                .withRate(new BigDecimal(8))
                .withReason("reason")
                .withInterestDate(SampleInterestDate.validDefaults())
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withSpecificDailyAmount(new BigDecimal(10))
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
                .containsExactlyInAnyOrder(
                        "rate : may not be provided when interest type is 'no interest'",
                        "reason : may not be provided when interest type is 'no interest'",
                        "interestDate : may not be provided when interest type is 'no interest'",
                        "interestBreakdown : may not be provided when interest type is 'no interest'",
                        "specificDailyAmount : may not be provided when interest type is 'no interest'");
    }

    @Test
    public void withStandardInterestType_shouldBeSuccessful() {
        //given
        Interest interest = SampleInterest
                .builder()
                .withType(STANDARD)
                .withRate(new BigDecimal(8))
                .withReason(null)
                .withInterestBreakdown(null)
                .withInterestDate(SampleInterestDate.validDefaults())
                .withSpecificDailyAmount(null)
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void withStandardInterestType_shouldBeInvalidWhenMandatoryFieldsMissing() {
        //given
        Interest interest = SampleInterest
                .builder()
                .withType(STANDARD)
                .withRate(null)
                .withReason(null)
                .withInterestDate(null)
                .withInterestBreakdown(null)
                .withSpecificDailyAmount(null)
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
                .containsExactlyInAnyOrder(
                        "rate : may not be null when interest type is 'standard'",
                        "interestDate : may not be null when interest type is 'standard'");
    }

    @Test
    public void withStandardInterestType_shouldBeInvalidWhenUnexpectedFieldsProvided() {
        //given
        Interest interest = SampleInterest
                .builder()
                .withType(STANDARD)
                .withRate(new BigDecimal(8))
                .withReason("reason")
                .withInterestDate(SampleInterestDate.validDefaults())
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withSpecificDailyAmount(new BigDecimal(10))
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
                .containsExactlyInAnyOrder(
                        "reason : may not be provided when interest type is 'standard'",
                        "interestBreakdown : may not be provided when interest type is 'standard'",
                        "specificDailyAmount : may not be provided when interest type is 'standard'");
    }

    @Test
    public void withStandardInterestType_shouldBeInvalidWithInvalidInterestDate() {
        //given
        InterestDate invalidInterestDate = SampleInterestDate
                .builder()
                .withType(CUSTOM)
                .withReason(null)
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(STANDARD)
                .withRate(new BigDecimal(8))
                .withReason(null)
                .withInterestBreakdown(null)
                .withInterestDate(invalidInterestDate)
                .withSpecificDailyAmount(null)
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
                .containsExactly("interestDate : reason : may not be null");
    }

    @Test
    public void withDifferentInterestType_shouldBeSuccessful() {
        //given
        Interest interest = SampleInterest
                .builder()
                .withType(DIFFERENT)
                .withRate(new BigDecimal(8))
                .withReason("reason")
                .withInterestDate(SampleInterestDate.validDefaults())
                .withInterestBreakdown(null)
                .withSpecificDailyAmount(null)
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void withDifferentInterestType_shouldBeInvalidWhenMandatoryFieldsMissing() {
        //given
        Interest interest = SampleInterest
                .builder()
                .withType(DIFFERENT)
                .withRate(null)
                .withReason(null)
                .withInterestDate(null)
                .withInterestBreakdown(null)
                .withSpecificDailyAmount(null)
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
                .containsExactlyInAnyOrder(
                        "rate : may not be null when interest type is 'different'",
                        "reason : may not be null when interest type is 'different'",
                        "interestDate : may not be null when interest type is 'different'");
    }

    @Test
    public void withDifferentInterestType_shouldBeInvalidWhenUnexpectedFieldsProvided() {
        //given
        Interest interest = SampleInterest
                .builder()
                .withType(DIFFERENT)
                .withRate(new BigDecimal(8))
                .withReason("reason")
                .withInterestDate(SampleInterestDate.validDefaults())
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withSpecificDailyAmount(new BigDecimal(10))
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
                .containsExactlyInAnyOrder(
                        "interestBreakdown : may not be provided when interest type is 'different'",
                        "specificDailyAmount : may not be provided when interest type is 'different'");
    }

    @Test
    public void withDifferentInterestType_shouldBeInvalidWithInvalidInterestDate() {
        //given
        InterestDate invalidInterestDate = SampleInterestDate
                .builder()
                .withType(CUSTOM)
                .withReason(null)
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(DIFFERENT)
                .withRate(new BigDecimal(8))
                .withReason("reason")
                .withInterestDate(invalidInterestDate)
                .withInterestBreakdown(null)
                .withSpecificDailyAmount(null)
                .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
                .containsExactly("interestDate : reason : may not be null");
    }

    @Test
    public void withBreakdownInterestTypeAndSettleOrJudgmentEndDateType_shouldBeSuccessfulWhenRateProvided() {
        InterestDate interestDateOfTypeSettledOrJudgment = SampleInterestDate
                .builder()
                .withEndDateType(InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT)
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(BREAKDOWN)
                .withInterestDate(interestDateOfTypeSettledOrJudgment)
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withReason(null)
                .withRate(new BigDecimal(8))
                .withSpecificDailyAmount(null)
                .build();

        Set<String> errors = validate(interest);

        assertThat(errors).isEmpty();
    }

    @Test
    public void withBreakdownInterestTypeAndSettleOrJudgmentEndDateType_shouldBeSuccessfulWhenSpecDailyAmntProvided() {
        InterestDate interestDateOfTypeSettledOrJudgment = SampleInterestDate
                .builder()
                .withEndDateType(InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT)
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(BREAKDOWN)
                .withInterestDate(interestDateOfTypeSettledOrJudgment)
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withReason(null)
                .withRate(null)
                .withSpecificDailyAmount(new BigDecimal(10))
                .build();

        Set<String> errors = validate(interest);

        assertThat(errors).isEmpty();
    }

    @Test
    public void withBreakdownInterestTypeAndSubmissionEndDateType_shouldBeSuccessful() {
        InterestDate interestDateOfTypeSubmission = SampleInterestDate
                .builder()
                .withEndDateType(InterestDate.InterestEndDateType.SUBMISSION)
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(BREAKDOWN)
                .withInterestDate(interestDateOfTypeSubmission)
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withReason(null)
                .withRate(null)
                .withSpecificDailyAmount(null)
                .build();

        Set<String> errors = validate(interest);

        assertThat(errors).isEmpty();
    }

    @Test
    public void withBreakdownInterestTypeAndSettleOrJudgmentEndDateType_shouldBeInvalidWhenMandatoryFieldsMissing() {
        InterestDate interestDateOfTypeSettledOrJudgment = SampleInterestDate
                .builder()
                .withEndDateType(InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT)
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(BREAKDOWN)
                .withInterestDate(interestDateOfTypeSettledOrJudgment)
                .withInterestBreakdown(null)
                .withReason(null)
                .withRate(null)
                .withSpecificDailyAmount(null)
                .build();

        Set<String> errors = validate(interest);

        assertThat(errors).containsExactlyInAnyOrder(
                "interestBreakdown : may not be null when interest type is 'breakdown'",
                "rate : may not be null when interest type is 'breakdown'",
                "specificDailyAmount : may not be null when interest type is 'breakdown'");
    }

    @Test
    public void withBreakdownInterestTypeAndSubmissionEndDateType_shouldBeInvalidWhenMandatoryFieldsMissing() {
        InterestDate interestDateOfTypeSubmission = SampleInterestDate
                .builder()
                .withEndDateType(InterestDate.InterestEndDateType.SUBMISSION)
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(BREAKDOWN)
                .withInterestDate(interestDateOfTypeSubmission)
                .withInterestBreakdown(null)
                .withReason(null)
                .withRate(null)
                .withSpecificDailyAmount(null)
                .build();

        Set<String> errors = validate(interest);

        assertThat(errors).containsExactly("interestBreakdown : may not be null when interest type is 'breakdown'");
    }

    @Test
    public void withBreakdownInterestTypeAndSettleOrJudgmentEndDateType_shouldBeInvalidWhenUnexpectedFieldsProvided() {
        InterestDate interestDateOfTypeSettledOrJudgment = SampleInterestDate
                .builder()
                .withEndDateType(InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT)
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(BREAKDOWN)
                .withInterestDate(interestDateOfTypeSettledOrJudgment)
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withReason("reason")
                .withRate(new BigDecimal(8))
                .withSpecificDailyAmount(null)
                .build();

        Set<String> errors = validate(interest);

        assertThat(errors).containsExactly("reason : may not be provided when interest type is 'breakdown'");
    }

    @Test
    public void withBreakdownInterestTypeAndSubmissionEndDateType_shouldBeInvalidWhenUnexpectedFieldsProvided() {
        InterestDate interestDateOfTypeSubmission = SampleInterestDate
                .builder()
                .withEndDateType(InterestDate.InterestEndDateType.SUBMISSION)
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(BREAKDOWN)
                .withInterestDate(interestDateOfTypeSubmission)
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withReason("reason")
                .withRate(new BigDecimal(8))
                .withSpecificDailyAmount(new BigDecimal(10))
                .build();

        Set<String> errors = validate(interest);

        assertThat(errors).containsExactlyInAnyOrder(
                "reason : may not be provided when interest type is 'breakdown'",
                "rate : may not be provided when interest type is 'breakdown'",
                "specificDailyAmount : may not be provided when interest type is 'breakdown'");
    }

    @Test
    public void withBreakdownInterestType_shouldBeInvalidWithInvalidBreakdown() {
        InterestDate interestDateOfTypeSubmission = SampleInterestDate
                .builder()
                .withEndDateType(InterestDate.InterestEndDateType.SUBMISSION)
                .build();
        InterestBreakdown invalidInterestBreakdown = SampleInterestBreakdown
                .builder()
                .withTotalAmount(null)
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(BREAKDOWN)
                .withInterestDate(interestDateOfTypeSubmission)
                .withInterestBreakdown(invalidInterestBreakdown)
                .withReason(null)
                .withRate(null)
                .withSpecificDailyAmount(null)
                .build();

        Set<String> errors = validate(interest);

        assertThat(errors).containsExactly("interestBreakdown : totalAmount : must not be null");
    }

    @Test
    public void withBreakdownInterestType_shouldBeInvalidWithInvalidInterestDate() {
        InterestDate invalidInterestDate = SampleInterestDate
                .builder()
                .withType(SUBMISSION)
                .withReason("reason")
                .build();
        Interest interest = SampleInterest
                .builder()
                .withType(BREAKDOWN)
                .withInterestDate(invalidInterestDate)
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withReason(null)
                .withRate(new BigDecimal(10))
                .withSpecificDailyAmount(null)
                .build();

        Set<String> errors = validate(interest);

        assertThat(errors)
                .containsExactlyInAnyOrder(
                        "interestDate : reason : may not be provided when type is 'submission'",
                        "interestDate : date : may not be provided when type is 'submission'");
    }

    @Test
    public void withBreakdownInterestType_shouldBeInvalidWithNullInterestDate() {
        Interest interest = SampleInterest
                .builder()
                .withType(BREAKDOWN)
                .withInterestDate(null)
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withReason(null)
                .withRate(null)
                .withSpecificDailyAmount(null)
                .build();

        Set<String> errors = validate(interest);

        assertThat(errors).containsExactly("interestDate : may not be null when interest type is 'breakdown'");
    }
}
