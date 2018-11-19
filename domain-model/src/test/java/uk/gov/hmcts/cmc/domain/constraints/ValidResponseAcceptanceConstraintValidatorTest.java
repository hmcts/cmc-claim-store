package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

import javax.validation.ConstraintValidatorContext;

import static java.math.BigDecimal.TEN;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.CCJ;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.bySetDate;

@RunWith(MockitoJUnitRunner.class)
public class ValidResponseAcceptanceConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private ValidResponseAcceptanceConstraintValidator validator = new ValidResponseAcceptanceConstraintValidator();

    @Test
    public void isValid() {
        ResponseAcceptation responseAcceptation = ResponseAcceptation.builder()
            .amountPaid(TEN)
            .claimantPaymentIntention(bySetDate())
            .courtDetermination(CourtDetermination.builder()
                .courtDecision(bySetDate())
                .courtPaymentIntention(bySetDate())
                .disposableIncome(TEN)
                .decisionType(CLAIMANT)
                .build())
            .formaliseOption(CCJ)
            .build();

        assertTrue(validator.isValid(responseAcceptation, validatorContext));
    }
}
