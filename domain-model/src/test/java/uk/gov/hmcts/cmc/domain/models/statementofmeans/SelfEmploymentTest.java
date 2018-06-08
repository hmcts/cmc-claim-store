package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class SelfEmploymentTest {

    @Test
    public void shouldBeSuccessfulValidationForUnemployed() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .amountYouOwe(BigDecimal.ONE)
            .reason("Whatever")
            .build();

        SelfEmployment selfEmployment = SelfEmployment.builder()
            .jobTitle("CEO")
            .annualTurnover(BigDecimal.TEN)
            .onTaxPayments(onTaxPayments)
            .build();
        //when
        Set<String> response = validate(selfEmployment);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForInvalidOnTaxPayments() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .amountYouOwe(BigDecimal.ONE)
            .build();

        SelfEmployment selfEmployment = SelfEmployment.builder()
            .jobTitle("CEO")
            .annualTurnover(BigDecimal.TEN)
            .onTaxPayments(onTaxPayments)
            .build();
        //when
        Set<String> response = validate(selfEmployment);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("onTaxPayments.reason : may not be empty");
    }

    @Test
    public void shouldBeInvalidForBlankJobTitle() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .amountYouOwe(BigDecimal.ONE)
            .reason("Whatever")
            .build();

        SelfEmployment selfEmployment = SelfEmployment.builder()
            .jobTitle("")
            .annualTurnover(BigDecimal.TEN)
            .onTaxPayments(onTaxPayments)
            .build();
        //when
        Set<String> response = validate(selfEmployment);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("jobTitle : may not be empty");
    }

    @Test
    public void shouldBeInvalidForNullAnnualTurnover() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .amountYouOwe(BigDecimal.ONE)
            .reason("Whatever")
            .build();

        SelfEmployment selfEmployment = SelfEmployment.builder()
            .jobTitle("CEO")
            .onTaxPayments(onTaxPayments)
            .build();
        //when
        Set<String> response = validate(selfEmployment);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("annualTurnover : may not be null");
    }

    @Test
    public void shouldBeInvalidForAnnualTurnoverIsWithMoreThanTwoFractions() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .amountYouOwe(BigDecimal.ONE)
            .reason("Whatever")
            .build();

        SelfEmployment selfEmployment = SelfEmployment.builder()
            .jobTitle("CEO")
            .annualTurnover(BigDecimal.valueOf(0.123f))
            .onTaxPayments(onTaxPayments)
            .build();
        //when
        Set<String> response = validate(selfEmployment);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("annualTurnover : can not be more than 2 fractions");
    }
}
