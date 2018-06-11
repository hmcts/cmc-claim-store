package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class OnTaxPaymentsTest {
    public static OnTaxPayments.OnTaxPaymentsBuilder newSampleOfOnTaxPaymentsBuilder() {
        return OnTaxPayments.builder()
                .amountYouOwe(BigDecimal.ONE)
                .reason("Whatever");
    }
        
    @Test
    public void shouldBeSuccessfulValidationForOnTaxPayments() {
        //given
        OnTaxPayments onTaxPayments = newSampleOfOnTaxPaymentsBuilder().build();
        //when
        Set<String> response = validate(onTaxPayments);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidWhenAllFieldsAreNullInOnTaxPayments() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .build();
        //when
        Set<String> response = validate(onTaxPayments);
        //then
        assertThat(response).hasSize(2);
    }

    @Test
    public void shouldBeInvalidWhenAmountYouOweIsNull() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .reason("Whatever")
            .build();
        //when
        Set<String> response = validate(onTaxPayments);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("amountYouOwe : may not be null");
    }

    @Test
    public void shouldBeInvalidWhenReasonIsNull() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .amountYouOwe(BigDecimal.ONE)
            .build();
        //when
        Set<String> response = validate(onTaxPayments);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("reason : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenAmountYouOweIsWithMoreThanTwoFractions() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .amountYouOwe(BigDecimal.valueOf(0.123f))
            .reason("Whatever")
            .build();
        //when
        Set<String> response = validate(onTaxPayments);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("amountYouOwe : can not be more than 2 fractions");
    }
}
