package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import javax.validation.ConstraintValidatorContext;

import java.time.LocalDate;

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
    public void shouldBeValidWhenPaymentIntentionIsImmediatelyDefendantIsIndividualAndSoMIsNotPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentIntention(validImmediatelyBuilder().build()).build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenPaymentIntentionIsImmediatelyDefendantIsSoleTraderAndSoMIsNotPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .defendant(SampleParty.builder().soleTrader())
            .paymentIntention(validImmediatelyBuilder().build()).build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenPaymentIntentionIsImmediatelyDefendantIsIndividualAndSoMIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentIntention(validImmediatelyBuilder().build())
            .statementOfMeans(StatementOfMeans.builder().reason("my reason").build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenPaymentIntentionIsImmediatelyDefendantIsSoleTraderAndSoMIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentIntention(validImmediatelyBuilder().build())
            .statementOfMeans(StatementOfMeans.builder().reason("my reason").build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenPaymentIntentionIsSpecDateDefendantIsIndividualAndSoMIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentIntention(
                PaymentIntention.builder()
                    .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
                    .paymentDate(LocalDate.now().plusDays(10)).build()
            ).statementOfMeans(StatementOfMeans.builder().reason("my reason").build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenPaymentIntentionIsInstalmentsDefendantIsIndividualAndSoMIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentIntention(
                PaymentIntention.builder()
                    .paymentOption(PaymentOption.INSTALMENTS)
                    .repaymentPlan(SampleRepaymentPlan.builder().build()).build()
            ).statementOfMeans(StatementOfMeans.builder().reason("my reason").build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenPaymentIntentionIsNotImmediatelyDefendantIsIndividualAndSoMIsNotPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentIntention(
                PaymentIntention.builder()
                    .paymentOption(PaymentOption.INSTALMENTS)
                    .repaymentPlan(SampleRepaymentPlan.builder().build()).build()
            ).build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
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
    public void shouldInvalidWhenOnlyPaymentDeclarationIsPopulatedAndItIsValid() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentDeclaration(SamplePaymentDeclaration.validDefaults())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldInvalidWhenOnlyPaymentIntentionIsPopulatedAndItIsValid() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentIntention(SamplePaymentIntention.immediately())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    private static PaymentIntention.PaymentIntentionBuilder validImmediatelyBuilder() {
        return PaymentIntention.builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .paymentDate(LocalDate.now().plusDays(5));
    }

    private static PartAdmissionResponse.PartAdmissionResponseBuilder builder() {
        return PartAdmissionResponse.builder()
            .freeMediation(YesNoOption.YES)
            .moreTimeNeeded(YesNoOption.NO)
            .defendant(SampleParty.builder().individual());
    }
}
