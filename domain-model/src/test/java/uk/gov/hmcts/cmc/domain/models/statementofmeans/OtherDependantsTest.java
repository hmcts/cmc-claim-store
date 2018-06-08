package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class OtherDependantsTest {

    @Test
    public void shouldBeSuccesfulValidationForOtherDependants() {
        //given
        OtherDependants otherDependants = OtherDependants.builder()
            .details("details")
            .numberOfPeople(3)
            .build();
        //when
        Set<String> response = validate(otherDependants);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        OtherDependants otherDependants = OtherDependants.builder().build();
        //when
        Set<String> errors = validate(otherDependants);
        //then
        assertThat(errors)
            .hasSize(2);
    }

    @Test
    public void shouldBeInvalidForBlankDetailsAndZeroNumberOfPeople() {
        //given
        OtherDependants otherDependants = OtherDependants.builder()
            .details("")
            .numberOfPeople(0)
            .build();
        //when
        Set<String> errors = validate(otherDependants);
        //then
        assertThat(errors)
            .hasSize(2);
    }

    @Test
    public void shouldBeInvalidForNullNumberOfPeople() {
        //given
        OtherDependants otherDependants = OtherDependants.builder()
            .details("details")
            .build();
        //when
        Set<String> errors = validate(otherDependants);
        //then
        assertThat(errors)
            .hasSize(1);
    }

    @Test
    public void shouldBeInvalidForZeroNumberOfPeople() {
        //given
        OtherDependants otherDependants = OtherDependants.builder()
            .numberOfPeople(0)
            .details("details")
            .build();
        //when
        Set<String> errors = validate(otherDependants);
        //then
        assertThat(errors)
            .hasSize(1);
    }

    @Test
    public void shouldBeInvalidForBlankDependantDetails() {
        //given
        OtherDependants otherDependants = OtherDependants.builder()
            .details("")
            .numberOfPeople(3)
            .build();
        //when
        Set<String> errors = validate(otherDependants);
        //then
        assertThat(errors)
            .hasSize(1);
    }
}
