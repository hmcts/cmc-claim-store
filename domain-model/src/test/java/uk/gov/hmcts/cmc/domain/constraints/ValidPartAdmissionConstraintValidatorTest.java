package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
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
    public void shouldBeValidWhenPaymentOptionIsEmpty() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(null)
            .paymentDate(LocalDate.now())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenTypeIsImmediatelyAndPaymentDateIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .paymentDate(LocalDate.now())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsImmediatelyAndPaymentDateIsNotNull() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .paymentDate(null)
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsImmediatelyAndRepaymentPlanIsNotNull() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsImmediatelyAndStatementOfMeansIsNotNull() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenTypeIsBySetDateAndPaymentDateIsValid() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .paymentDate(LocalDate.now().plusDays(3))
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsBySetDateAndEverythingElseIsNotPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsBySetDateAndRepaymentPlanIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .paymentDate(LocalDate.now().plusDays(3))
            .statementOfMeans(StatementOfMeans.builder().build())
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenTypeIsInstalmentsAndRepaymentPlanIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsInstalmentsAndEverythingElseIsNotPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsInstalmentsAndEverythingElseIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .paymentDate(LocalDate.now().plusDays(1))
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    private static PartAdmissionResponse.PartAdmissionResponseBuilder builder() {
        return PartAdmissionResponse.builder()
            .freeMediation(YesNoOption.YES)
            .moreTimeNeeded(YesNoOption.NO)
            .defendant(SampleParty.builder().individual());
    }
}
