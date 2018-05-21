package uk.gov.hmcts.cmc.domain.models;

import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestBreakdown;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class InterestTest {

    @Test
    @Ignore // To be enabled after new validators are implemented
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
                "type : may not be null",
                "rate : may not be null"
            );
    }

    @Test
    public void shouldBeSuccessfulValidationForStandardInterestType() {
        //given
        Interest interest = SampleInterest.builder()
            .withType(Interest.InterestType.STANDARD)
            .withRate(new BigDecimal(8))
            .withReason("reason")
            .build();
        //when
        Set<String> errors = validate(interest);
        //then
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
    public void shouldBeInvalidForBreakdownInterestTypeWithNullBreakdown() {
        Interest interest = SampleInterest.breakdownInterestBuilder()
            .withInterestBreakdown(null)
            .build();

        Set<String> errors = validate(interest);

        assertThat(errors).containsOnly("interestBreakdown : may not be null");
    }

    @Test
    public void shouldBeInvalidForBreakdownInterestTypeWithInvalidBreakdown() {
        Interest interest = SampleInterest.breakdownInterestBuilder()
            .withInterestBreakdown(
                SampleInterestBreakdown.builder()
                    .withTotalAmount(null)
                    .withExplanation(null)
                    .build()
            )
            .build();

        Set<String> errors = validate(interest);

        assertThat(errors).containsOnly(
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

}
