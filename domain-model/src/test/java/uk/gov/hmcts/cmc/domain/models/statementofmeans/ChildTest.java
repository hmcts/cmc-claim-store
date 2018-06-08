package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.UNDER_11;

public class ChildTest {

    @Test
    public void shouldBeSuccessfulValidationForChild() {
        //given
        Child child = Child.builder()
                .ageGroupType(UNDER_11)
                .numberOfChildren(2)
                .build();
        //when
        Set<String> response = validate(child);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        //given
        Child child = Child.builder().build();
        //when
        Set<String> errors = validate(child);
        //then
        assertThat(errors)
                .hasSize(2);
    }

    @Test
    public void shouldBeInvalidForNullAgeGroupType() {
        //given
        Child child = Child.builder()
                .numberOfChildren(2)
                .build();
        //when
        Set<String> errors = validate(child);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("ageGroupType : may not be null");
    }

    @Test
    public void shouldBeInvalidForNullNumberOfChildren() {
        //given
        Child child = Child.builder()
                .ageGroupType(UNDER_11)
                .build();
        //when
        Set<String> errors = validate(child);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("numberOfChildren : may not be null");
    }
}