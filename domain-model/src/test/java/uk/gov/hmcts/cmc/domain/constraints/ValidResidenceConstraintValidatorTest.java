package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidResidenceConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private final ValidResidenceConstraintValidator validator = new ValidResidenceConstraintValidator();

    @Before
    public void setUp() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(
            ConstraintValidatorContext.ConstraintViolationBuilder.class
        );

        when(builder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    }

    @Test
    public void shouldBeValidWhenNull() {
        assertIsValid(null);
    }

    @Test
    public void shouldBeValidWhenResidenceTypeIsOwnHomeAndOtherDetailsIsEmpty() {
        buildAndAssertIsValidForType(Residence.ResidenceType.OWN_HOME);
    }

    @Test
    public void shouldBeValidWhenResidenceTypeIsJointOwnHomeAndOtherDetailsIsEmpty() {
        buildAndAssertIsValidForType(Residence.ResidenceType.JOINT_OWN_HOME);
    }

    @Test
    public void shouldBeValidWhenResidenceTypeIsCouncilHomeAndOtherDetailsIsEmpty() {
        buildAndAssertIsValidForType(Residence.ResidenceType.COUNCIL_OR_HOUSING_ASSN_HOME);
    }

    @Test
    public void shouldBeValidWhenResidenceTypeIsPrivateRentalAndOtherDetailsIsEmpty() {
        buildAndAssertIsValidForType(Residence.ResidenceType.PRIVATE_RENTAL);
    }

    @Test
    public void shouldBeInvalidWhenResidenceTypeIsOwnHomeAndOtherDetailsIsNotEmpty() {
        buildAndAssertIsInvalidForType(Residence.ResidenceType.OWN_HOME);
    }

    @Test
    public void shouldBeInvalidWhenResidenceTypeIsJointOwnHomeAndOtherDetailsIsNotEmpty() {
        buildAndAssertIsInvalidForType(Residence.ResidenceType.JOINT_OWN_HOME);
    }

    @Test
    public void shouldBeInvalidWhenResidenceTypeIsCouncilHomeAndOtherDetailsIsNotEmpty() {
        buildAndAssertIsInvalidForType(Residence.ResidenceType.COUNCIL_OR_HOUSING_ASSN_HOME);
    }

    @Test
    public void shouldBeInvalidWhenResidenceTypeIsPrivateRentalAndOtherDetailsIsNotEmpty() {
        buildAndAssertIsInvalidForType(Residence.ResidenceType.PRIVATE_RENTAL);
    }

    @Test
    public void shouldBeValidWhenResidenceTypeIsOtherAndOtherDetailsIsPopulated() {
        assertIsValid(Residence.builder().type(Residence.ResidenceType.OTHER).otherDetail("my home").build());
    }

    @Test
    public void shouldBeInvalidWhenResidenceTypeIsOtherAndOtherDetailsIsNull() {
        assertIsInvalid(Residence.builder().type(Residence.ResidenceType.OTHER).otherDetail(null).build());
    }

    private void buildAndAssertIsValidForType(Residence.ResidenceType type) {
        assertIsValid(Residence.builder().type(type).build());
    }

    private void buildAndAssertIsInvalidForType(Residence.ResidenceType type) {
        assertIsInvalid(Residence.builder().type(type).otherDetail("only OTHER is valid").build());
    }

    private void assertIsValid(Residence residence) {
        assertThat(validator.isValid(residence, validatorContext)).isTrue();
    }

    private void assertIsInvalid(Residence residence) {
        assertThat(validator.isValid(residence, validatorContext)).isFalse();
    }
}
