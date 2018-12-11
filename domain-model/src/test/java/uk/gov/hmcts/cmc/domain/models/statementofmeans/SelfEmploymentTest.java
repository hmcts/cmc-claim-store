package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class SelfEmploymentTest {
    private static SelfEmployment.SelfEmploymentBuilder newSampleOfSelfEmploymentBuilder() {
        return SelfEmployment.builder()
            .jobTitle("CEO")
            .annualTurnover(BigDecimal.TEN)
            .onTaxPayments(OnTaxPaymentsTest.newSampleOfOnTaxPaymentsBuilder().build());
    }

    @Test
    public void shouldBeSuccessfulValidationForSelfEmployed() {
        SelfEmployment selfEmployment = newSampleOfSelfEmploymentBuilder().build();

        assertThat(validate(selfEmployment)).isEmpty();
    }

    @Test
    public void shouldBeInvalidForInvalidOnTaxPayments() {
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .amountYouOwe(BigDecimal.ONE)
            .build();

        SelfEmployment selfEmployment = newSampleOfSelfEmploymentBuilder()
            .onTaxPayments(onTaxPayments)
            .build();

        assertThat(validate(selfEmployment))
            .hasSize(1)
            .contains("onTaxPayments.reason : may not be empty");
    }

    @Test
    public void shouldBeInvalidForBlankJobTitle() {
        SelfEmployment selfEmployment = newSampleOfSelfEmploymentBuilder()
            .jobTitle("")
            .build();

        assertThat(validate(selfEmployment))
            .hasSize(1)
            .contains("jobTitle : may not be empty");
    }

    @Test
    public void shouldBeInvalidForNullAnnualTurnover() {
        SelfEmployment selfEmployment = newSampleOfSelfEmploymentBuilder()
            .annualTurnover(null)
            .build();

        assertThat(validate(selfEmployment))
            .hasSize(1)
            .contains("annualTurnover : may not be null");
    }

    @Test
    public void shouldBeInvalidForAnnualTurnoverIsWithMoreThanTwoFractions() {
        SelfEmployment selfEmployment = newSampleOfSelfEmploymentBuilder()
            .annualTurnover(BigDecimal.valueOf(0.123f))
            .build();

        assertThat(validate(selfEmployment))
            .hasSize(1)
            .contains("annualTurnover : can not be more than 2 fractions");
    }

    @Test
    public void shouldBeInvalidForNegativeAnnualTurnover() {
        SelfEmployment selfEmployment = newSampleOfSelfEmploymentBuilder()
            .annualTurnover(BigDecimal.valueOf(-1))
            .build();

        assertThat(validate(selfEmployment))
            .hasSize(1)
            .contains("annualTurnover : must be greater than or equal to 0");
    }
}
