package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.UNDER_11;

public class DependantTest {
    public static Dependant.DependantBuilder newSampleOfDependantBuilder() {
        return Dependant.builder()
                .numberOfMaintainedChildren(1)
                .children(Collections.singletonList(ChildTest.newSampleOfChildBuilder().build()))
                .otherDependants(OtherDependantsTest.newSampleOfOtherDependantsBuilder().build());
    }

    @Test
    public void shouldBeSuccessfulValidationForCorrectDependant() {
        //given
        Dependant dependant = newSampleOfDependantBuilder().build();
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
                .children(Collections.singletonList(invalidChild))
                .build();
        //when
        Set<String> errors = validate(dependant);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("children[0].numberOfChildren : must not be null");
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
                .contains("otherDependants.numberOfPeople : must not be null");
    }
}
