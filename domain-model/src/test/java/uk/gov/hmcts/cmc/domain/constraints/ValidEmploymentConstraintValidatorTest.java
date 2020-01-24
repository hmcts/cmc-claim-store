package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployed;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import java.math.BigDecimal;
import java.util.Collections;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidEmploymentConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private final ValidEmploymentConstraintValidator validator = new ValidEmploymentConstraintValidator();

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

        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }
}
