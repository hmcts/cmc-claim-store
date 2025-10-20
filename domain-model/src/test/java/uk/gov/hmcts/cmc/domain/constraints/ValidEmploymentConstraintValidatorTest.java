package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployed;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;
import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidEmploymentConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    private final ValidEmploymentConstraintValidator validator = new ValidEmploymentConstraintValidator();

    @Test
    public void shouldBeValidWhenNull() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenOnlyUnemployedIsPopulated() {
        Employment model = Employment.builder()
            .unemployment(Unemployment.builder().unemployed(new Unemployed(1, 1)).build())
            .build();

        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenOnlySelfEmployedIsPopulated() {
        Employment model = Employment.builder()
            .selfEmployment(new SelfEmployment("job", BigDecimal.TEN, null))
            .build();

        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenOnlyEmployersIsPopulated() {
        Employment model = Employment.builder()
            .employers(Collections.singletonList(Employer.builder().jobTitle("job").name("company").build()))
            .build();

        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenEmployersAndSelfEmployedArePopulated() {
        Employment model = Employment.builder()
            .selfEmployment(new SelfEmployment("job", BigDecimal.TEN, null))
            .employers(Collections.singletonList(Employer.builder().jobTitle("job").name("company").build()))
            .build();

        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenAllFieldsPopulated() {
        Employment model = Employment.builder()
            .selfEmployment(new SelfEmployment("job", BigDecimal.TEN, null))
            .employers(Collections.singletonList(Employer.builder().jobTitle("job").name("company").build()))
            .unemployment(Unemployment.builder().retired(true).build())
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class));

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }
}
