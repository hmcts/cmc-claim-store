package uk.gov.hmcts.cmc.domain.models.statementofmeans;


import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class UnemploymentTest {
    @Test
    public void shouldBeSuccessfulValidationForUnemployment() {
        //given
        Unemployed unemployed = Unemployed.builder()
            .numberOfMonths(10)
            .numberOfYears(2)
            .build();
        Unemployment unemployment = Unemployment.builder()
            .unemployed(unemployed)
            .retired(true)
            .other("other")
            .build();
        //when
        Set<String> response = validate(unemployment);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeSuccessfulForInvalidFields() {
        //given
        Unemployment unemployment = Unemployment.builder()
            .build();
        //when
        Set<String> response = validate(unemployment);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidWhenUnemployedIsInvalid() {
        //given
        Unemployed unemployed = Unemployed.builder()
            .numberOfMonths(10)
            .build();
        Unemployment unemployment = Unemployment.builder()
            .unemployed(unemployed)
            .retired(true)
            .other("other")
            .build();
        //when
        Set<String> response = validate(unemployment);
        System.out.println(response);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("unemployed.numberOfYears : may not be null");
    }
}
