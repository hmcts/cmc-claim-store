package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class EmploymentTest {
    @Test
    public void shouldBeSuccessfulValidationForEmploymentWhenUnemployed() {
        //given
        Unemployed unemployed = Unemployed.builder()
            .numberOfMonths(10)
            .numberOfYears(2)
            .build();

        Unemployment unemployment = Unemployment.builder()
            .unemployed(unemployed)
            .build();

        Employment employment = Employment.builder()
            .unemployment(unemployment)
            .build();

        //when
        Set<String> response = validate(employment);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeSuccessfulValidationForEmploymentWhenEmployed() {
        SelfEmployment selfEmployment = SelfEmployment.builder()
            .jobTitle("CEO")
            .annualTurnover(BigDecimal.TEN)
            .onTaxPayments(OnTaxPayments.builder()
                .amountYouOwe(BigDecimal.ONE)
                .reason("Whatever")
                .build())
            .build();

        Employer employer = Employer.builder()
            .jobTitle("My job")
            .name("My Company")
            .build();

        Employment employment = Employment.builder()
            .employers(Collections.singletonList(employer))
            .selfEmployment(selfEmployment)
            .build();

        //when
        Set<String> response = validate(employment);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeValidWhenAllFieldsAreNull() {
        //given
        Employment employment = Employment.builder().build();
        //when
        Set<String> response = validate(employment);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidWhenEmployerIsInvalid() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .reason("Whatever")
            .build();

        SelfEmployment selfEmployment = SelfEmployment.builder()
            .annualTurnover(BigDecimal.TEN)
            .onTaxPayments(onTaxPayments)
            .build();

        Employer employer = Employer.builder()
            .jobTitle("My job")
            .build();

        Employment employment = Employment.builder()
            .employers(Collections.singletonList(employer))
            .selfEmployment(selfEmployment)
            .build();

        //when
        Set<String> response = validate(employment);
        //then
        assertThat(response)
            .hasSize(3)
            .contains("employers[0].name : may not be empty")
            .contains("selfEmployment.jobTitle : may not be empty");
    }
}
