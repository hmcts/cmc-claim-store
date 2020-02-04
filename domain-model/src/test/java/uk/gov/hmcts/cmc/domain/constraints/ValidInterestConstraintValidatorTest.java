package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ValidInterestConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private final ValidInterestConstraintValidator validator = new ValidInterestConstraintValidator();

    @Test
    public void shouldBeValidForNullInterest() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidForInterestWithNullType() {
        Interest interest = SampleInterest.builder()
            .withType(null)
            .build();

        assertThat(validator.isValid(interest, validatorContext)).isTrue();
    }

}
