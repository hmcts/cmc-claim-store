package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidChildConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    private final ValidChildConstraintValidator validator = new ValidChildConstraintValidator();

    @Test
    public void shouldBeValidWhenModelIsNull() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenAgeGroupIsBetween11And15AndNumberOfChildrenLivingWithYouIsNotPopulated() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.BETWEEN_11_AND_15)
            .numberOfChildren(1)
            .build();

        assertThat(validator.isValid(child, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenAgeGroupIsUnder11AndNumberOfChildrenLivingWithYouIsNotPopulated() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.UNDER_11)
            .numberOfChildren(1)
            .build();

        assertThat(validator.isValid(child, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenAgeGroupIsUnder11AndNumberOfChildrenLivingWithYouIsZero() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.UNDER_11)
            .numberOfChildren(1)
            .numberOfChildrenLivingWithYou(0)
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(child, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenAgeGroupIsUnder11AndNumberOfChildrenLivingWithYouIsPositive() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.UNDER_11)
            .numberOfChildren(2)
            .numberOfChildrenLivingWithYou(1)
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(child, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenAgeGroupIsBetween11And15AndNumberOfChildrenLivingWithYouIsZero() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.BETWEEN_11_AND_15)
            .numberOfChildren(1)
            .numberOfChildrenLivingWithYou(0)
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(child, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenAgeGroupIsBetween11And15AndNumberOfChildrenLivingWithYouIsPositive() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.BETWEEN_11_AND_15)
            .numberOfChildren(2)
            .numberOfChildrenLivingWithYou(3)
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(child, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenAgeGroupIsBetween16An19AndNumberOfChildrenLivingWithYouIsZero() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.BETWEEN_16_AND_19)
            .numberOfChildren(2)
            .numberOfChildrenLivingWithYou(0)
            .build();

        assertThat(validator.isValid(child, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenAgeGroupIsBetween16An19AndNumberOfChildrenLivingWithYouIsPositive() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.BETWEEN_16_AND_19)
            .numberOfChildren(2)
            .numberOfChildrenLivingWithYou(1)
            .build();

        assertThat(validator.isValid(child, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenAgeGroupIsBetween16An19AndNumberOfChildrenLivingWithYouIsEqNumberOfChildren() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.BETWEEN_16_AND_19)
            .numberOfChildren(2)
            .numberOfChildrenLivingWithYou(2)
            .build();

        assertThat(validator.isValid(child, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenAgeGroupIsBetween16An19AndNumberOfChildrenLivingWithYouIsEqNumberOfChildrenBothZero() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.BETWEEN_16_AND_19)
            .numberOfChildren(0)
            .numberOfChildrenLivingWithYou(0)
            .build();

        assertThat(validator.isValid(child, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenAgeGroupIsBetween16An19AndNumberOfChildrenLivingWithYouIsNotPopulated() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.BETWEEN_16_AND_19)
            .numberOfChildren(1)
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(child, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenAgeGroupIsBetween16An19AndNoOfChildrenLivingWithYouIsGreaterThanNoOfChildren() {
        Child child = Child.builder()
            .ageGroupType(Child.AgeGroupType.BETWEEN_16_AND_19)
            .numberOfChildren(2)
            .numberOfChildrenLivingWithYou(3)
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(child, validatorContext)).isFalse();
    }
}
