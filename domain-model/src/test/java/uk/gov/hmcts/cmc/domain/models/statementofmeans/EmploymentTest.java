package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class EmploymentTest {
    public static Employment.EmploymentBuilder newSampleOfEmploymentBuilder() {
        return Employment.builder()
                .employers(Arrays.asList(EmployerTest.newSampleOfEmployerBuilder().build()))
                .selfEmployment(SelfEmploymentTest.newSampleOfSelfEmploymentBuilder().build())
                .unemployment(UnemploymentTest.newSampleOfUnemploymentBuilder().build());
    }

    @Test
    public void shouldBeSuccessfulValidationForEmployment() {
        //given
        OnTaxPayments onTaxPayments = OnTaxPayments.builder()
            .amountYouOwe(BigDecimal.ONE)
            .reason("Whatever")
            .build();

        Unemployed unemployed = Unemployed.builder()
            .numberOfMonths(10)
            .numberOfYears(2)
            .build();

        Unemployment unemployment = Unemployment.builder()
            .unemployed(unemployed)
            .retired(true)
            .other("other")
            .build();

        SelfEmployment selfEmployment = SelfEmployment.builder()
            .jobTitle("CEO")
            .annualTurnover(BigDecimal.TEN)
            .onTaxPayments(onTaxPayments)
            .build();

        Employer employer = Employer.builder()
            .jobTitle("My job")
            .name("My Company")
            .build();

        Employment employment = Employment.builder()
            .employers(Arrays.asList(employer))
            .selfEmployment(selfEmployment)
            .unemployment(unemployment)
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

        Unemployed unemployed = Unemployed.builder()
            .numberOfMonths(10)
            .build();

        Unemployment unemployment = Unemployment.builder()
            .unemployed(unemployed)
            .retired(true)
            .build();

        SelfEmployment selfEmployment = SelfEmployment.builder()
            .annualTurnover(BigDecimal.TEN)
            .onTaxPayments(onTaxPayments)
            .build();

        Employer employer = Employer.builder()
            .jobTitle("My job")
            .build();

        Employment employment = Employment.builder()
            .employers(Arrays.asList(employer))
            .selfEmployment(selfEmployment)
            .unemployment(unemployment)
            .build();

        //when
        Set<String> response = validate(employment);
        //then
        assertThat(response)
            .hasSize(4)
            .contains("employers[0].name : may not be empty")
            .contains("unemployment.unemployed.numberOfYears : may not be null")
            .contains("selfEmployment.jobTitle : may not be empty");
    }
}
