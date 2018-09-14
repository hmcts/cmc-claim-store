package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FormaliseResponseAcceptanceServiceTest {
    private FormaliseResponseAcceptanceService formaliseResponseAcceptanceService;
    private Claim sampleClaim = SampleClaim.getWithDefaultResponse();
    private ResponseAcceptation responseAcceptation = ResponseAcceptation.builder().build();
    private CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder().build();
    @Mock
    private OffersService offersService;
    @Mock
    private CountyCourtJudgmentService countyCourtJudgmentService;
    private static final String AUTH = "AUTH";

    @Before
    public void before() {
        formaliseResponseAcceptanceService = new FormaliseResponseAcceptanceService(countyCourtJudgmentService,
            offersService);

    }

    @Test
    public void testFormaliseCCJWithDefendantPaymentIntentionAccepted () {
       ResponseAcceptation responseAcceptation =
            SampleClaimantResponse.ClaimantResponseAcceptation.builder().withFormaliseOption(FormaliseOption.CCJ).build();
        formaliseResponseAcceptanceService.formalise(sampleClaim, claimantResponseAcceptation, AUTH);

    }

}
