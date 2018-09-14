package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThatCode;


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

        ArgumentCaptor CCJObject = ArgumentCaptor.forClass(CountyCourtJudgment.class);

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);
        CountyCourtJudgment countyCourtJudgment = (CountyCourtJudgment) CCJObject.capture();

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            any(CountyCourtJudgment.class),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(true));
        assertThat(countyCourtJudgment.getRepaymentPlan()).isNotNull();
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
}
