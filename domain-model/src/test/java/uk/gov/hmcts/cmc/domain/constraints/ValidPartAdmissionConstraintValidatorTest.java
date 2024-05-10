package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ValidPartAdmissionConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private final ValidPartAdmissionConstraintValidator validator = new ValidPartAdmissionConstraintValidator();

    @Test
    public void shouldBeValidWhenInputIsNull() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenPaymentIntentionIsImmediatelyDefendantIsIndividualAndSoMIsNotPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.immediately())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenPaymentIntentionIsImmediatelyDefendantIsSoleTraderAndSoMIsNotPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .defendant(SampleParty.builder().soleTrader())
            .paymentIntention(SamplePaymentIntention.immediately())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenPaymentIntentionIsImmediatelyDefendantIsIndividualAndSoMIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.immediately())
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenPaymentIntentionIsImmediatelyDefendantIsSoleTraderAndSoMIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .defendant(SampleParty.builder().soleTrader())
            .paymentIntention(SamplePaymentIntention.immediately())
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenPaymentIntentionIsSpecDateDefendantIsIndividualAndSoMIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.bySetDate())
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenPaymentIntentionIsInstalmentsDefendantIsIndividualAndSoMIsPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.instalments())
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenPaymentIntentionIsNotImmediatelyDefendantIsIndividualAndSoMIsNotPopulated() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.instalments())
            .build();

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
            .paymentDeclaration(SamplePaymentDeclaration.builder().build())
            .paymentIntention(SamplePaymentIntention.immediately())
            .build();

        assertThat(validator.isValid(partAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldInvalidWhenOnlyPaymentDeclarationIsPopulatedAndItIsValid() {
        PartAdmissionResponse partAdmissionResponse = builder()
            .paymentDeclaration(SamplePaymentDeclaration.builder().build())
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

    private static PartAdmissionResponse.PartAdmissionResponseBuilder builder() {
        return PartAdmissionResponse.builder()
            .freeMediation(YesNoOption.YES)
            .moreTimeNeeded(YesNoOption.NO);
    }
}
