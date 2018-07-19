package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidPartAdmissionConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private ValidPartAdmissionConstraintValidator validator = new ValidPartAdmissionConstraintValidator();

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
    public void shouldBeValidWhenInputIsNull() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenBothPaymentDeclarationAndPaymentIntentionAreNotPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenBothPaymentDeclarationAndPaymentIntentionArePopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentDeclaration(SamplePaymentDeclaration.validDefaults())
            .paymentIntention(SamplePaymentIntention.immediately())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenOnlyPaymentDeclarationIsPopulatedAndItIsValid() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentDeclaration(SamplePaymentDeclaration.validDefaults())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenOnlyPaymentIntentionIsPopulatedAndItIsValid() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentIntention(SamplePaymentIntention.immediately())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    private static PartAdmissionResponse.PartAdmissionResponseBuilder builder() {
        return PartAdmissionResponse.builder()
            .freeMediation(YesNoOption.YES)
            .moreTimeNeeded(YesNoOption.NO)
            .defendant(SampleParty.builder().individual());
    }
}
