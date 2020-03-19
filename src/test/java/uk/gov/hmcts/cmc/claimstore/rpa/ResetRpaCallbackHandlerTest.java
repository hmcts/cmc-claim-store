package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ResetRpaCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport.RoboticsNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResetRpaCallbackHandlerTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String EXTERNAL_ID = "external id";
    private static final String CASE_NAME = "case name";
    private static final String REFERENCE = "reference";
    private static final String REFERENCE_KEY = "previousServiceCaseReference";
    private static final Long ID = 1L;
    private static final String EVENT_ID = "ResetRPA";
    private static final String RPA_EVENT_TYPE_KEY = "RPAEventType";
    private static final String CLAIM = "CLAIM";
    private static final String MORE_TIME = "MORE_TIME";
    private static final String CCJ = "CCJ";
    private static final String DEFENDANT_RESPONSE = "DEFENDANT_RESPONSE";
    private static final String PAID_IN_FULL = "PAID_IN_FULL";
    private static final String INVALID = "INVALID";
    private CallbackRequest callbackRequest;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private RoboticsNotificationService roboticsNotificationService;

    private ResetRpaCallbackHandler resetRpaCallbackHandler;

    @Before
    public void setup() {
        resetRpaCallbackHandler = new ResetRpaCallbackHandler(caseDetailsConverter,
            caseMapper, roboticsNotificationService);
        when(caseMapper.to(any(Claim.class))).thenReturn(getCcdCase());
        when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(getCcdCaseMap());
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(getClaim());
    }

    @Test
    public void shouldResetRpaWhenValidClaimEventSent() {
        when(roboticsNotificationService.rpaClaimNotification(anyString())).thenReturn(REFERENCE);
        callbackRequest = getCallbackRequest(CLAIM);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaClaimNotification(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaWhenValidMoreTimeEventSent() {
        callbackRequest = getCallbackRequest(MORE_TIME);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaMoreTimeNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaWhenValidCcjEventSent() {
        callbackRequest = getCallbackRequest(CCJ);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaCCJNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaWhenValidDefendantResponseEventSent() {
        callbackRequest = getCallbackRequest(DEFENDANT_RESPONSE);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaResponseNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaWhenValidPaidInFullEventSent() {
        callbackRequest = getCallbackRequest(PAID_IN_FULL);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaPIFNotifications(eq(REFERENCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowBadRequestExceptionWhenInvalidEventSent() {
        callbackRequest = getCallbackRequest(INVALID);
        resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
    }

    private CCDCase getCcdCase() {
        return CCDCase.builder()
            .externalId(EXTERNAL_ID)
            .previousServiceCaseReference(REFERENCE)
            .caseName(CASE_NAME)
            .id(ID)
            .build();
    }

    private Map<String, Object> getCcdCaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(EXTERNAL_ID, getCcdCase());
        map.put(REFERENCE_KEY, REFERENCE);
        return map;
    }

    private Claim getClaim() {
        return Claim.builder()
            .referenceNumber(REFERENCE)
            .externalId(EXTERNAL_ID)
            .build();
    }

    private CallbackRequest getCallbackRequest(String rpaEventType) {
        Map<String, Object> data = new HashMap<>();
        data.put(RPA_EVENT_TYPE_KEY, rpaEventType);
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(data).build())
            .eventId(EVENT_ID)
            .build();
    }

    private CallbackParams getCallbackParams(CallbackRequest callbackRequest) {
        return CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }
}
