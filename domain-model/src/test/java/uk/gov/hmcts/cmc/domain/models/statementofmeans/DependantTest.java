package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.UNDER_11;

public class DependantTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectDependant() {
        //given
        Child child = Child.builder()
                .ageGroupType(UNDER_11)
                .numberOfChildren(2)
                .build();
        OtherDependants otherDependants = OtherDependants.builder()
                .details("Details")
                .numberOfPeople(1)
                .build();
        Dependant dependant = Dependant.builder()
                .children(Arrays.asList(child))
                .numberOfMaintainedChildren(1)
                .otherDependants(otherDependants)
                .build();
        //when
        Set<String> response = validate(dependant);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeValidForAllNullFields() {
        //given
        Dependant dependant = Dependant.builder().build();
        //when
        Set<String> errors = validate(dependant);
        //then
        assertThat(errors)
                .hasSize(0);
    }

    @Test
    public void shouldBeInvalidForInvalidChildren() {
        //given
        Child invalidChild = Child.builder()
                .ageGroupType(UNDER_11)
                .build();
        Dependant dependant = Dependant.builder()
                .children(Arrays.asList(invalidChild))
                .build();
        //when
        Set<String> errors = validate(dependant);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("children[0].numberOfChildren : may not be null");
    }

    @Test
    public void shouldBeInvalidForInvalidOtherDependants() {
        //given
        OtherDependants invalidOtherDependants = OtherDependants.builder()
                .details("Details")
                .build();
        Dependant dependant = Dependant.builder()
                .otherDependants(invalidOtherDependants)
                .build();
        //when
        Set<String> errors = validate(dependant);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("otherDependants.numberOfPeople : may not be null");
    }
}