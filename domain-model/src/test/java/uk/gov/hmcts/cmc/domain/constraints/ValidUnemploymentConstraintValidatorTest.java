package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployed;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidUnemploymentConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private final ValidUnemploymentConstraintValidator validator = new ValidUnemploymentConstraintValidator();

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

        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }
}
