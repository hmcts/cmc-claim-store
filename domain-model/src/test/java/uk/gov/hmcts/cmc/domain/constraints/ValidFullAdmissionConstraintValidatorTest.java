package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.time.LocalDate;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidFullAdmissionConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private ValidFullAdmissionConstraintValidator validator = new ValidFullAdmissionConstraintValidator();

    @Before
    public void setup() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(
            ConstraintValidatorContext.ConstraintViolationBuilder.class
        );

        when(builder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenInputIsNull() {
        validator.isValid(null, validatorContext);
    }

    @Test
    public void shouldBeValidWhenTypeIsImmediatelyAndNothingElseIsPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsImmediatelyAndPaymentDateIsNotNull() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .paymentDate(LocalDate.now().plusDays(10))
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsImmediatelyAndRepaymentPlanIsNotNull() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenTypeIsBySetDateAndPaymentDateIsValid() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentOption(PaymentOption.FULL_BY_SPECIFIED_DATE)
            .paymentDate(LocalDate.now().plusDays(3))
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    @Ignore // as I have wrong context. I cannot be mocked if I want to get accurate validation
    public void shouldBeInvalidWhenTypeIsBySetDateAndPaymentDateIsInThePast() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentOption(PaymentOption.FULL_BY_SPECIFIED_DATE)
            .paymentDate(LocalDate.now().minusDays(3))
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsBySetDateAndEverythingElseIsNotPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentOption(PaymentOption.FULL_BY_SPECIFIED_DATE)
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenTypeIsInstalmentsAndRepaymentPlanIsPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsInstalmentsAndEverythingElseIsNotPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenTypeIsInstalmentsAndEverythingElseIsPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .paymentDate(LocalDate.now().plusDays(1))
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenTypeIsInstalmentsAndRepaymentPlanIsInvalid() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now().minusDays(10)).build())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isFalse();
    }

    private static FullAdmissionResponse.FullAdmissionResponseBuilder builder() {
        return FullAdmissionResponse.builder()
            .freeMediation(YesNoOption.YES)
            .moreTimeNeeded(YesNoOption.NO)
            .statementOfTruth(StatementOfTruth.builder().build())
            .defendant(SampleParty.builder().individual())
            .statementOfMeans(StatementOfMeans.builder().build());
    }

}
