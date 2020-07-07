package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;


import static org.mockito.Mockito.*;

public class HWFPartRemissionCallbackHandlerTest {
    @Mock
    List<Role> ROLES;
    @Mock
    List<CaseEvent> EVENTS;
    @Mock
    Logger logger;
    @Mock
    CaseDetailsConverter caseDetailsConverter;
    @Mock
    DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;
    @Mock
    CaseMapper caseMapper;
    @InjectMocks
    HWFPartRemissionCallbackHandler hWFFeeRemittedCallbackHandler;

    @Mock
    CallbackParams callbackParams;

    @Mock
    CallbackRequest callbackRequest ;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCallbacks() throws Exception {
        Map<CallbackType, Callback> result = hWFFeeRemittedCallbackHandler.callbacks();
        Assert.assertEquals(new HashMap<CallbackType, Callback>() {{
            put(CallbackType.ABOUT_TO_START, null);
        }}, result);
    }

    @Test
    public void testHandledEvents() throws Exception {
        Claim claim = SampleClaim.builder().build();
        when(callbackParams.getRequest()).thenReturn(callbackRequest);
        doNothing().when(callbackRequest.getCaseDetails());//.thenReturn(callbackRequest);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        hWFFeeRemittedCallbackHandler.updateFeeRemitted(callbackParams);
        List<CaseEvent> result = hWFFeeRemittedCallbackHandler.handledEvents();
        Assert.assertEquals(Arrays.<CaseEvent>asList(CaseEvent.CREATE_CASE), result);
    }

    @Test
    public void testRegister() throws Exception {
        hWFFeeRemittedCallbackHandler.register(new HashMap<String, CallbackHandler>() {{
            put("String", null);
        }});
    }

    @Test
    public void testUpdateFeeRemitted() throws Exception {
        Claim claim = SampleClaim.builder().build();
        when(callbackParams.getRequest()).thenReturn(callbackRequest);
        doNothing().when(callbackRequest.getCaseDetails());//.thenReturn(callbackRequest);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        hWFFeeRemittedCallbackHandler.updateFeeRemitted(callbackParams);
        List<CaseEvent> result = hWFFeeRemittedCallbackHandler.handledEvents();
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme
