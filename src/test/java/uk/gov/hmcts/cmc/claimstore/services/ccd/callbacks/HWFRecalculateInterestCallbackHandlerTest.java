package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType.SUBMISSION;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest.breakdownInterestBuilder;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest.noInterestBuilder;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest.standardInterestBuilder;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate.builder;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate.customDateToSettledOrJudgement;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate.submissionToSettledOrJudgement;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HWF Recalculate Interest CallbackHandler Test")
class HWFRecalculateInterestCallbackHandlerTest {

    @InjectMocks
    HWFRecalculateInterestCallbackHandler hwfRecalculateInterestCallbackHandler;

    @Mock
    private FeesClient feesClient;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Captor
    ArgumentCaptor<CCDCase> ccdCaseCaptor;

    private Claim claim;
    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;

    private static final String FEE_EVENT = "issue";
    private static final String FEE_CHANNEL = "online";

    private static final String INTEREST_NOT_CLAIMED = "Recalculation is not required as interest is not claimed";
    private static final String NOT_HWF_CLAIM = "Recalculation is not required for non HWF Claims";
    private static final String INTEREST_NOT_TILL_JUDGEMENT = "Recalculation is not required as interest is only " +
        "claimed to the date of claim submission";

    @BeforeEach
    public void setUp() {
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(CaseEvent.HWF_PART_REMISSION_GRANTED.getValue())
            .build();
        claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.builder().build())
                    .build())
            .build();
    }

    @Test
    void shouldGiveErrorIfClaimantHasNotRequestedForInterestTillFinalJudgement() {
        Claim claim = SampleClaim.getClaimWithFullAdmission();
        ClaimData interestTillSubmission  = claim.getClaimData().toBuilder().interest(
            standardInterestBuilder().withInterestDate(builder().withEndDateType(SUBMISSION).build()).build()).build();
        shouldValidateClaim(claim.toBuilder().claimData(interestTillSubmission).build(), INTEREST_NOT_TILL_JUDGEMENT);
    }

    @Test
    void shouldGiveErrorIfClaimDoesNotNeedHWF() {
        Claim claim = SampleClaim.getClaimWithFullAdmission();
        ClaimData claimDataWithoutFeeNumber = claim.getClaimData().toBuilder().helpWithFeesNumber(null).build();
        shouldValidateClaim(claim.toBuilder().claimData(claimDataWithoutFeeNumber).build(), NOT_HWF_CLAIM);
    }

    @Test
    void shouldGiveErrorIfClaimantHasNotClaimedInterest() {
        Claim claim = SampleClaim.getClaimWithFullAdmission();
        ClaimData claimWithoutInterest = claim.getClaimData().toBuilder().interest(noInterestBuilder().build()).build();
        shouldValidateClaim(claim.toBuilder().claimData(claimWithoutInterest).build(), INTEREST_NOT_CLAIMED);
    }

    private void shouldValidateClaim(Claim claim, String expectedErrorMessage) {
        callbackParams = CallbackParams.builder().type(ABOUT_TO_START).request(callbackRequest).build();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        AboutToStartOrSubmitCallbackResponse response = callHandler();
        assertEquals(1, response.getErrors().size());
        assertEquals(expectedErrorMessage, response.getErrors().get(0));
    }

    private AboutToStartOrSubmitCallbackResponse callHandler() {
        return (AboutToStartOrSubmitCallbackResponse) hwfRecalculateInterestCallbackHandler.handle(callbackParams);
    }

    @Test
    void shouldCalculateInterestAndFeesAndUpdateTheTotalAmountClaimedForCustomFromDate() {
        Interest interest = standardInterestBuilder().withInterestDate(customDateToSettledOrJudgement()).build();
        calculateAndValidate(interest, "4", "1000", "5103");
    }

    @Test
    void shouldCalculateInterestAndFeesAndUpdateTheTotalAmountClaimedForSubmissionDateAsFromDate() {
        Interest interest = standardInterestBuilder().withInterestDate(submissionToSettledOrJudgement()).build();
        calculateAndValidate(interest, "0", "1000", "5099");
    }

    private void calculateAndValidate(Interest interest, String expectedInterest, String fee, String totalAmount) {
        callbackParams = CallbackParams.builder().type(ABOUT_TO_SUBMIT).request(callbackRequest).build();
        ClaimData claimData = claim.getClaimData().toBuilder().interest(interest).build();
        claim = claim.toBuilder().claimData(claimData).build();
        FeeLookupResponseDto feeDTO = FeeLookupResponseDto.builder().feeAmount(TEN).build();
        when(feesClient.lookupFee(anyString(), anyString(), any(BigDecimal.class))).thenReturn(feeDTO);
        when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(new HashMap<>());
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        callHandler();
        verify(caseDetailsConverter).convertToMap(ccdCaseCaptor.capture());
        CCDCase ccdCase = ccdCaseCaptor.getValue();

        assertEquals(fee, ccdCase.getFeeAmountInPennies());
        assertEquals(totalAmount, ccdCase.getTotalAmount());
        assertEquals(expectedInterest, ccdCase.getCurrentInterestAmount());
        assertEquals(LocalDateTime.now().toLocalDate(), ccdCase.getLastInterestCalculationDate().toLocalDate());
    }

    @Test
    void shouldCalculateBreakdownInterestAndFeesAndUpdateTheTotalAmountClaimedWithFixedRate() {
        claim = claim.toBuilder().issuedOn(LocalDate.now().minusDays(1)).build();
        Interest interest = breakdownInterestBuilder().withRate(TEN).build();
        calculateAndValidate(interest, "4001", "1000", "9100");
    }

    @Test
    void shouldCalculateBreakdownInterestAndFeesAndUpdateTheTotalAmountClaimedCustomWithSpecificDailyRate() {
        claim = claim.toBuilder().issuedOn(LocalDate.now().minusDays(1)).build();
        Interest interest = breakdownInterestBuilder().withSpecificDailyAmount(BigDecimal.ONE).build();
        calculateAndValidate(interest, "4100", "1000", "9199");
    }

}
