package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class EmployerTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectEmployer() {
        //given
        Employer employer = Employer.builder()
            .jobTitle("My job")
            .employerName("My Company")
            .build();
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
            .employerName("My Company")
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
                .employerName("My Company")
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
            .contains("employerName : may not be empty");
    }

    @Test
    public void shouldBeInvalidForBlankEmployerName() {
        //given
        Employer employer = Employer.builder()
                .jobTitle("My job")
                .employerName("")
                .build();
        //when
        Set<String> errors = validate(employer);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("employerName : may not be empty");
    }
}
