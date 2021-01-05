package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.HWFCaseWorkerRespondSlaCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleHwfClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HWF Part Remission Callback Handler")
class HWFPartRemissionCallbackHandlerTest {
    private static final String PART_REMISSION_EQUAL_ERROR_MESSAGE =
        "Remitted fee is same as the total fee. For full remission, "
            + "please cancel and select the next step as \"Full remission HWF-granted\"";
    private static final String PART_REMISSION_IS_MORE_ERROR_MESSAGE = "Remitted fee should be less than the total fee";
    private static final String AUTHORISATION = "Bearer: aaaa";
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private HWFPartRemissionCallbackHandler handler;
    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    @Mock
    private DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private HWFCaseWorkerRespondSlaCalculator hwfCaseWorkerRespondSlaCalculator;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    private CCDCase ccdCase;

    @BeforeEach
    public void setUp() {
        ccdCase = getCCDCase();
        handler = new HWFPartRemissionCallbackHandler(caseDetailsConverter, deadlineCalculator, caseMapper,
            eventProducer, userService, hwfCaseWorkerRespondSlaCalculator);
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(CaseEvent.HWF_PART_REMISSION_GRANTED.getValue())
            .build();
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    private CCDCase getCCDCase() {
        return CCDCase.builder()
            .previousServiceCaseReference("CMC")
            .interestType(CCDInterestType.STANDARD)
            .submittedOn(LocalDateTime.now())
            .lastInterestCalculationDate(LocalDateTime.now())
            .feeAmountInPennies("2")
            .totalAmount("10")
            .build();
    }

    @Test
    void shouldUpdateFeeRemitted() {
        Claim claim = SampleClaim.getClaimWithFullAdmission();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(hwfCaseWorkerRespondSlaCalculator.calculate(ccdCase.getSubmittedOn())).thenReturn(LocalDate.now());
        Map<String, Object> mappedCaseData = new HashMap<>();
        mappedCaseData.put("feeRemitted", 4000);
        when(caseDetailsConverter.convertToMap(caseMapper.to(claim))).thenReturn(mappedCaseData);
        when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(mappedCaseData);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        Map<String, Object> data = response.getData();
        assertThat(data).containsEntry("feeRemitted", 4000);
    }

    @Test
    void feeRemittedIsMoreThanFee() {
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("CMC")
            .interestType(CCDInterestType.STANDARD)
            .submittedOn(LocalDateTime.now())
            .lastInterestCalculationDate(LocalDateTime.now())
            .feeAmountInPennies("2")
            .feeRemitted("20")
            .totalAmount("2")
            .build();
        Claim claim = SampleClaim.getClaimWhenFeeRemittedIsMoreThanFee();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(hwfCaseWorkerRespondSlaCalculator.calculate(ccdCase.getSubmittedOn())).thenReturn(LocalDate.now());
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        Assertions.assertNotNull(response.getErrors());
        assertThat(response.getErrors().get(0)).isEqualTo(PART_REMISSION_IS_MORE_ERROR_MESSAGE);
    }

    @Test
    void feeRemittedIsEqualToFee() {
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("CMC")
            .interestType(CCDInterestType.STANDARD)
            .submittedOn(LocalDateTime.now())
            .lastInterestCalculationDate(LocalDateTime.now())
            .feeAmountInPennies("2")
            .feeRemitted("2")
            .totalAmount("2")
            .build();
        Claim claim = SampleClaim.getClaimWhenFeeRemittedIsEqualToFee();
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(hwfCaseWorkerRespondSlaCalculator.calculate(ccdCase.getSubmittedOn())).thenReturn(LocalDate.now());
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        Assertions.assertNotNull(response.getErrors());
        assertThat(response.getErrors().get(0)).isEqualTo(PART_REMISSION_EQUAL_ERROR_MESSAGE);
    }

    @Test
    void shouldStartHwfClaimUpdatePostOperations() {
        Claim claim = SampleHwfClaim.getDefaultHwfPending();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();

        User mockUser = mock(User.class);
        when(mockUser.getAuthorisation()).thenReturn(AUTHORISATION);
        when(userService.getUser(anyString())).thenReturn(mockUser);
        when(mockUser.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getFullName()).thenReturn("TestUser");

        SubmittedCallbackResponse response
            = (SubmittedCallbackResponse) handler.handle(callbackParams);

        String data = response.getConfirmationBody();

        assertThat(claim).isNotNull();
    }
}
