package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

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

    private final ValidFullAdmissionConstraintValidator validator = new ValidFullAdmissionConstraintValidator();

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
        FullAdmissionResponse fullAdmissionResponse = builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.immediately())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenPaymentIntentionIsImmediatelyDefendantIsSoleTraderAndSoMIsNotPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .defendant(SampleParty.builder().soleTrader())
            .paymentIntention(SamplePaymentIntention.immediately())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenPaymentIntentionIsImmediatelyDefendantIsIndividualAndSoMIsPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.immediately())
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenPaymentIntentionIsImmediatelyDefendantIsSoleTraderAndSoMIsPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .defendant(SampleParty.builder().soleTrader())
            .paymentIntention(SamplePaymentIntention.immediately())
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenPaymentIntentionIsSpecDateDefendantIsIndividualAndSoMIsPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.bySetDate())
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenPaymentIntentionIsInstalmentsDefendantIsIndividualAndSoMIsPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.instalments())
            .statementOfMeans(StatementOfMeans.builder().build())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenPaymentIntentionIsNotImmediatelyDefendantIsIndividualAndSoMIsNotPopulated() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(SamplePaymentIntention.instalments())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isFalse();
    }

    @Test
    public void shouldInvalidWhenOnlyPaymentIntentionIsPopulatedAndItIsValid() {
        FullAdmissionResponse fullAdmissionResponse = builder()
            .paymentIntention(SamplePaymentIntention.immediately())
            .build();

        assertThat(validator.isValid(fullAdmissionResponse, validatorContext)).isTrue();
    }

    private static FullAdmissionResponse.FullAdmissionResponseBuilder builder() {
        return FullAdmissionResponse.builder()
            .freeMediation(YesNoOption.YES)
            .moreTimeNeeded(YesNoOption.NO);
    }
}
