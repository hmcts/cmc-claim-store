package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class InterestTest {

    private static final BigDecimal STANDARD_INTEREST_RATE = new BigDecimal(8);
    private static final String REASON = "Any reason";
    private static final String MAY_NOT_BE_NULL_OR_EMPTY = "may not be null or empty";
    private static final BigDecimal NON_STANDARD_INTEREST_RATE = new BigDecimal(10);

    @Test
    public void shouldHaveValidationMessagesWhenInterestAttributesAreNull() {
        //given
        Interest interest = SampleInterest.builder()
            .withType(null)
            .withRate(null)
            .withReason(null)
            .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
            .hasSize(2)
            .contains(
                "type : may not be null or empty"
            );
    }

    @Test
    public void shouldBeSuccessfulValidationForStandardInterest() {
        //given
        Interest interest = SampleInterest.standard();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeInValidWithStandardInterestWithInvalidCustomInterestDateValues() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
            .withType(InterestDate.InterestDateType.CUSTOM)
            .withReason("")
            .withDate(null)
            .build();

        //given
        Interest interest = SampleInterest.standardWithSpecificInterestDate(interestDate);

        //when
        Set<String> errors = validate(interest);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains("interestDate.Date or interest.interestDate.reason : may not be null or empty");

    }

    @Test
    public void shouldBeInvalidForBreakdownInterestWithNullBreakdown() {
        Interest interest = SampleInterest.breakdownInterestBuilder()
            .withInterestBreakdown(null)
            .withType(Interest.InterestType.BREAKDOWN)
            .build();

        Set<String> errors = validate(interest);

        assertThat(errors).containsOnly("interestBreakdown : may not be null");
    }

    @Test
    public void shouldBeInvalidForBreakdownInterestWithInvalidBreakdown() {
        Interest interest = SampleInterest.breakdownInterestBuilder()
            .withType(Interest.InterestType.BREAKDOWN)
            .withInterestBreakdown(
                SampleInterestBreakdown.builder()
                    .withTotalAmount(null)
                    .withExplanation(null)
                    .build()
            )
            .build();

        Set<String> errors = validate(interest);

        assertThat(errors).hasSize(2).containsOnly(
            "interestBreakdown : totalAmount : may not be null",
            "interestBreakdown : explanation : may not be empty"
        );
    }

    @Test
    public void shouldBeValidForBreakdownInterestWithValidBreakdown() {
        Interest interest = SampleInterest.breakdownInterestBuilder()
            .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
            .build();

        Set<String> errors = validate(interest);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForNoInterestType() {
        //given
        Interest interest = SampleInterest.builder()
            .withType(Interest.InterestType.NO_INTEREST)
            .withRate(null)
            .withReason(null)
            .build();
        //when
        Set<String> errors = validate(interest);

        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnValidationMessageWithDifferentInterestForCustomTypeAndBlankReason() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
            .withType(InterestDate.InterestDateType.CUSTOM)
            .withReason("")
            .build();

        Interest interest = SampleInterest.builder()
            .withType(Interest.InterestType.DIFFERENT)
            .withRate(new BigDecimal(10))
            .withReason(REASON)
            .withInterestDate(interestDate)
            .build();

        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("interestDate.Date or interest.interestDate.reason : may not be null or empty");
    }

    @Test
    public void shouldBeValidWithDifferentInterestWithInterestOnSubmission() {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
            .withType(InterestDate.InterestDateType.SUBMISSION)
            .withReason("")
            .withDate(null)
            .build();
        //given
        Interest interest = SampleInterest.builder()
            .withType(Interest.InterestType.DIFFERENT)
            .withRate(new BigDecimal(10))
            .withReason(REASON)
            .withInterestDate(interestDate)
            .build();
        //when
        Set<String> errors = validate(interest);

        //then
        assertThat(errors).isEmpty();

    }

    @Test
    public void shouldBeInValidWithNullRateOfInterestOnDifferentRate() {
        //given
        Interest interest = SampleInterest.differentInterest(null, REASON);
        //when
        Set<String> errors = validate(interest);

        //then
        assertThat(errors).hasSize(1).contains("rate or reason : " + MAY_NOT_BE_NULL_OR_EMPTY);
    }

    @Test
    public void shouldBeInValidWithDifferentRateAndNoReason() {
        //given
        Interest interest = SampleInterest.differentInterest(NON_STANDARD_INTEREST_RATE, null);

        //when
        Set<String> errors = validate(interest);

        //then
        assertThat(errors).hasSize(1).contains("rate or reason : " + MAY_NOT_BE_NULL_OR_EMPTY);
    }



    @Test
    public void shouldBeInvalidWhenBreakdownInterestAndContinueToClaimAmountAndInterest() {
        final String templateMessage = "either rate or specific amount should be claimed";
        Interest interest = SampleInterest.breakdownInterestBuilder()
            .withRate(new BigDecimal(8))
            .withSpecificDailyAmount(new BigDecimal(1000)).build();

        //when
        Set<String> errors = validate(interest);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains("rate : either rate or specific amount should be claimed");
    }

    @Test
    public void shouldBeValidWithDifferentRateWithRateAndReason() {
        Interest interest = SampleInterest.differentInterest(NON_STANDARD_INTEREST_RATE, "Some reason");

        //when
        Set<String> errors = validate(interest);

        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnInValidWithNoInterestDateAndStandardInterestRate() {
        String fieldName = "interestDate";
        Interest interest = SampleInterest.standardWithNoInterestDate();

        //when
        Set<String> errors = validate(interest);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains("interestDate : may not be null or empty");

    }

    @Test
    public void shouldBeInValidWithZeroRateOfInterestOnDifferentRate() {
        String message = "has to be greater than zero value";
        Interest interest = SampleInterest.differentInterest(BigDecimal.ZERO, "some reason");

        //when
        Set<String> errors = validate(interest);

        //then
        assertThat(errors)
            .hasSize(1)
            .contains("rate : has to be greater than zero value");
    }


    @Test
    public void shouldBeInvalidWhenBreakdownInterestAndContinueToClaimWithInterestOnly() {
        Interest interest = SampleInterest.breakdownInterestBuilder()
            .withRate(new BigDecimal(8)).build();

        //when
        Set<String> errors = validate(interest);

        //then
        assertThat(errors).isEmpty();
    }

}
