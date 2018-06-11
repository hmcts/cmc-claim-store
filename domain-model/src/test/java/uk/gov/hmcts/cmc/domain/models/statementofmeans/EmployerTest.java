package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class EmployerTest {
    public static Employer.EmployerBuilder newSampleOfEmployerBuilder() {
        return Employer.builder()
                .jobTitle("My job")
                .name("My Company");
    }

    @Test
    public void shouldBeSuccessfulValidationForCorrectEmployer() {
        //given
        Employer employer = newSampleOfEmployerBuilder().build();
        //when
        Set<String> response = validate(employer);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        Employer employer = Employer.builder().build();
        //when
        Set<String> errors = validate(employer);
        //then
        assertThat(errors)
            .hasSize(2);
    }

    @Test
    public void shouldBeInvalidForNullJobTitle() {
        //given
        Employer employer = Employer.builder()
            .name("My Company")
            .build();
        //when
        Set<String> errors = validate(employer);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("jobTitle : may not be empty");
    }

    @Test
    public void shouldBeInvalidForBlankJobTitle() {
        //given
        Employer employer = Employer.builder()
            .name("My Company")
                .jobTitle("")
                .build();
        //when
        Set<String> errors = validate(employer);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("jobTitle : may not be empty");
    }

    @Test
    public void shouldBeInvalidForNullEmployerName() {
        //given
        Employer employer = Employer.builder()
            .jobTitle("My job")
            .build();
        //when
        Set<String> errors = validate(employer);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("name : may not be empty");
    }

    @Test
    public void shouldBeInvalidForBlankEmployerName() {
        //given
        Employer employer = Employer.builder()
            .jobTitle("My job")
            .name("")
            .build();
        //when
        Set<String> errors = validate(employer);
        //then
        assertThat(errors)
                .hasSize(1)
            .contains("name : may not be empty");
    }
}
