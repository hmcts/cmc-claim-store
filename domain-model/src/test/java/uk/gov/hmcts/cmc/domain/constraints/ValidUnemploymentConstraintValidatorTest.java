package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployed;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidUnemploymentConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    private final ValidUnemploymentConstraintValidator validator = new ValidUnemploymentConstraintValidator();

    @Test
    public void shouldBeValidWhenOnlyUnemployedPopulated() {
        Unemployment model = Unemployment.builder()
            .unemployed(new Unemployed(1, 1))
            .build();

        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenOnlyIsRetiredPopulated() {
        Unemployment model = Unemployment.builder().retired(true).build();

        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenOnlyOtherPopulated() {
        Unemployment model = Unemployment.builder().other("I am rich").build();

        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenAllFieldsPopulated() {
        Unemployment model = Unemployment.builder()
            .retired(true)
            .unemployed(new Unemployed(1, 1))
            .other("I am rich").build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }
}
