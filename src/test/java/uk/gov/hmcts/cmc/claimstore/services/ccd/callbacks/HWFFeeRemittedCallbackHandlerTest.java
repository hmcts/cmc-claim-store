package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.HWFCaseWorkerRespondSlaCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@ExtendWith(MockitoExtension.class)
@DisplayName("HWF Full Remiitance Fee calculator Handler ")
class HWFFeeRemittedCallbackHandlerTest {

    private static final String AUTHORISATION = "Bearer: aaaa";
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private HWFFeeRemittedCallbackHandler handler;
    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    @Mock
    private DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private UserService userService;

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(CaseEvent.HWF_FULL_REMISSION_GRANTED);

    @Mock
    private DefendantResponseNotificationService defendantResponseNotificationService;

    @Mock
    private HWFCaseWorkerRespondSlaCalculator hwfCaseWorkerRespondSlaCalculator;

    private UserDetails userDetails;

    private CCDCase ccdCase;

    @BeforeEach
    public void setUp() {
        ccdCase = getCCDCase();
        handler = new HWFFeeRemittedCallbackHandler(caseDetailsConverter, deadlineCalculator, caseMapper,
            eventProducer, userService, hwfCaseWorkerRespondSlaCalculator);
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(CaseEvent.HWF_FULL_REMISSION_GRANTED.getValue())
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

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(hwfCaseWorkerRespondSlaCalculator.calculate(ccdCase.getSubmittedOn())).thenReturn(LocalDate.now());
        Map<String, Object> mappedCaseData = new HashMap<>();
        mappedCaseData.put("feeRemitted", "4000");
        Claim claim = SampleClaim.getClaimWithFullAdmission();
        when(caseDetailsConverter.convertToMap(caseMapper.to(claim)))
            .thenReturn(mappedCaseData);
        when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(mappedCaseData);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler.handle(callbackParams);
        assertEquals("4000", response.getData().get("feeRemitted"));
    }

    @Test
    void getSupportedRoles() {
        List<Role> roleList = handler.getSupportedRoles();
        assertEquals(ROLES, roleList);
    }

    @Test
    void handledEvents() {
        List<CaseEvent> caseEventList = handler.handledEvents();
        assertEquals(EVENTS, caseEventList);
    }
}
