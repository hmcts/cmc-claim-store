package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

import javax.validation.ConstraintValidatorContext;

import static java.math.BigDecimal.TEN;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.CCJ;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.bySetDate;

@RunWith(MockitoJUnitRunner.class)
public class ValidResponseAcceptanceConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private ValidResponseAcceptanceConstraintValidator validator = new ValidResponseAcceptanceConstraintValidator();

    private ResponseAcceptation responseAcceptation = ResponseAcceptation.builder()
        .amountPaid(TEN)
        .claimantPaymentIntention(null)
        .courtDetermination(CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(bySetDate())
            .disposableIncome(TEN)
            .decisionType(CLAIMANT)
            .build())
        .formaliseOption(CCJ)
        .build();

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
    public void isValid() {
        assertTrue(validator.isValid(responseAcceptation, validatorContext));
    }
}
