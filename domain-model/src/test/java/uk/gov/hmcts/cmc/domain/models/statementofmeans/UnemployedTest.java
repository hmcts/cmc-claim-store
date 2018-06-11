package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class UnemployedTest {
    public static Unemployed.UnemployedBuilder newSampleOfUnemployedBuilder() {
        return Unemployed.builder()
                .numberOfYears(1)
                .numberOfMonths(5);
    }

    @Test
    public void shouldBeSuccessfulValidationForUnemployed() {
        //given
        Unemployed unemployed = newSampleOfUnemployedBuilder().build();
        //when
        Set<String> response = validate(unemployed);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        Unemployed unemployed = Unemployed.builder().build();
        //when
        Set<String> errors = validate(unemployed);
        //then
        assertThat(errors)
            .hasSize(2);
    }

    @Test
    public void shouldBeInvalidForNullNumberOfYearsField() {
        //given
        Unemployed unemployed = Unemployed.builder()
            .numberOfMonths(10)
            .build();
        //when
        Set<String> errors = validate(unemployed);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("numberOfYears : may not be null");
    }

    @Test
    public void shouldBeInvalidForNullNumberOfMonthsField() {
        //given
        Unemployed unemployed = Unemployed.builder()
            .numberOfYears(5)
            .build();
        //when
        Set<String> errors = validate(unemployed);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("numberOfMonths : may not be null");
    }
}
