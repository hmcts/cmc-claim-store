package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class FormaliseResponseAcceptanceServiceTest {
    private static final String AUTH = "AUTH";
    private FormaliseResponseAcceptanceService formaliseResponseAcceptanceService;
    @Mock
    private OffersService offersService;
    @Mock
    private CountyCourtJudgmentService countyCourtJudgmentService;

    @Before
    public void before() {
        formaliseResponseAcceptanceService = new FormaliseResponseAcceptanceService(countyCourtJudgmentService,
            offersService);
    }

    @Test
    public void testFormaliseCCJWithDefendantPaymentIntentionAccepted() {
        Response response = SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentOptionBySpecifiedDate();

        Claim claim = SampleClaim.getWithResponse(response);
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(FormaliseOption.CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            any(CountyCourtJudgment.class),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(true));
    }

    @Test
    public void testFormaliseDoesNothingWhenReferredToJudge() {
        Claim claim = SampleClaim.getWithDefaultResponse();
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(FormaliseOption.REFER_TO_JUDGE)
            .build();
        assertThatCode(() -> formaliseResponseAcceptanceService
            .formalise(claim, responseAcceptation, AUTH)).doesNotThrowAnyException();

    }

    @Test
    public void testFormaliseDoesNothingWhenResponseIsNotAcceptation() {
        Claim claim = SampleClaim.getWithDefaultResponse();
        ClaimantResponse response = ResponseRejection.builder().build();
        assertThatCode(() -> formaliseResponseAcceptanceService
            .formalise(claim, response, AUTH)).doesNotThrowAnyException();

    }
}
