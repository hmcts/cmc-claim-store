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
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;


@RunWith(MockitoJUnitRunner.class)
public class FormaliseResponseAcceptanceServiceTest {
    private static final String AUTH = "AUTH";
    private FormaliseResponseAcceptanceService formaliseResponseAcceptanceService;

    @Mock
    private OffersService offersService;

    @Mock
    private CountyCourtJudgmentService countyCourtJudgmentService;

    @Captor
    private ArgumentCaptor<CountyCourtJudgment> countyCourtJudgmentArgumentCaptor;

    @Before
    public void before() {
        formaliseResponseAcceptanceService = new FormaliseResponseAcceptanceService(countyCourtJudgmentService,
            offersService);
    }

    @Test(expected = IllegalStateException.class)
    public void formaliseWhenResponseNotPresent() {
        Claim claim = SampleClaim.builder().build();
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(FormaliseOption.CCJ)
            .build();
        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);
    }

    @Test
    public void formaliseCCJWithDefendantPaymentIntentionBySetDateAccepted() {
        PartAdmissionResponse response = SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentOptionBySpecifiedDate();

        LocalDate respondentPayingBySetDate = response
            .getPaymentIntention()
            .orElseThrow(IllegalStateException::new)
            .getPaymentDate()
            .orElseThrow(IllegalStateException::new);

        Claim claim = SampleClaim.getWithResponse(response);
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(FormaliseOption.CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(false));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getPayBySetDate()
            .orElseThrow(IllegalStateException::new))
            .isEqualTo(respondentPayingBySetDate);

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseCCJWithDefendantPaymentIntentionByInstalmentsAccepted() {
        PartAdmissionResponse response = SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentOptionInstallments();

        RepaymentPlan repaymentPlanOfDefendant = response
            .getPaymentIntention()
            .orElseThrow(IllegalStateException::new)
            .getRepaymentPlan()
            .orElseThrow(IllegalStateException::new);

        Claim claim = SampleClaim.getWithResponse(response);
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(FormaliseOption.CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(false));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getRepaymentPlan().orElseThrow(IllegalAccessError::new))
            .isEqualTo(repaymentPlanOfDefendant);

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseCCJWithCourtDeterminedIntentionAccepted() {
        Response response = SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentOptionBySpecifiedDate();

        Claim claim = SampleClaim.getWithResponse(response);
        PaymentIntention paymentIntention = SamplePaymentIntention.bySetDate();
        LocalDate appliedPaymentDate = paymentIntention.getPaymentDate().orElseThrow(IllegalStateException::new);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .courtDetermination(CourtDetermination
                .builder()
                .courtCalculatedPaymentIntention(paymentIntention)
                .build())
            .formaliseOption(FormaliseOption.CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(false));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getPayBySetDate()
            .orElseThrow(IllegalStateException::new))
            .isEqualTo(appliedPaymentDate);

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseCCJWithClaimantPaymentIntentionPresent() {
        Response response = SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentOptionBySpecifiedDate();

        Claim claim = SampleClaim.getWithResponse(response);

        PaymentIntention paymentIntentionByInstalments = SamplePaymentIntention.instalments();
        RepaymentPlan appliedRepaymentPlan = paymentIntentionByInstalments
            .getRepaymentPlan()
            .orElseThrow(IllegalStateException::new);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .claimantPaymentIntention(paymentIntentionByInstalments)
            .formaliseOption(FormaliseOption.CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(false));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getRepaymentPlan()
            .orElseThrow(IllegalStateException::new))
            .isEqualTo(appliedRepaymentPlan);

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseDoesNothingWhenReferredToJudge() {
        Claim claim = SampleClaim.getWithDefaultResponse();
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(FormaliseOption.REFER_TO_JUDGE)
            .build();
        assertThatCode(
            () -> formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH)
        ).doesNotThrowAnyException();

        verifyZeroInteractions(countyCourtJudgmentService);
        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseDoesNothingWhenResponseIsNotAcceptation() {
        Claim claim = SampleClaim.getWithDefaultResponse();
        ClaimantResponse response = ResponseRejection.builder().build();
        assertThatCode(
            () -> formaliseResponseAcceptanceService.formalise(claim, response, AUTH)
        ).doesNotThrowAnyException();

        verifyZeroInteractions(countyCourtJudgmentService);
        verifyZeroInteractions(offersService);
    }
}
