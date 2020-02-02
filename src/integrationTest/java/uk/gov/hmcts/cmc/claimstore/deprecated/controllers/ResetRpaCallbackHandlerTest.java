//package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;
//
//import com.google.common.collect.ImmutableMap;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
//import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
//import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
//import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
//import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ResetRpaCallbackHandler;
//import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
//import uk.gov.hmcts.cmc.domain.models.Claim;
//import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
//import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
//import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
//import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
//import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
//import static org.mockito.Mockito.when;
//import static org.junit.Assert.assertThat;
//
//import java.util.Collections;
//
//import static org.mockito.ArgumentMatchers.any;
//
//@RunWith(MockitoJUnitRunner.class)
//public class ResetRpaCallbackHandlerTest {
//
//    @Mock
//    private CaseDetailsConverter caseDetailsConverter;
//    @Mock
//    private CaseMapper caseMapper;
//
//    private CallbackRequest callbackRequest;
//
//    private CallbackParams callbackParams;
//
//    private static final String AUTHORISATION = "Bearer: test";
//    private Claim sampleClaimWithDefendantEmail =
//        SampleClaim.getDefaultWithoutResponse(SampleTheirDetails.DEFENDANT_EMAIL);
//
//    @InjectMocks
//    private ResetRpaCallbackHandler resetRpaCallbackHandler;
//
//    @Before
//    public void setUp() throws Exception {
//        resetRpaCallbackHandler = new ResetRpaCallbackHandler(caseDetailsConverter, caseMapper);
//        callbackRequest = CallbackRequest
//            .builder()
//            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
//            .eventId(CaseEvent.RESET_RPA.getValue())
//            .build();
//
//        callbackParams = CallbackParams.builder()
//            .type(CallbackType.ABOUT_TO_SUBMIT)
//            .request(callbackRequest)
//            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
//            .build();
//    }
//
//    @Test
//    public void shouldResetRPA() {
//        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
//            .thenReturn(sampleClaimWithDefendantEmail);
//        AboutToStartOrSubmitCallbackResponse response
//            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(callbackParams);
//        System.out.println("akriti: " + response.getData());
//        //assertThat(response.getData());
//    }
//}
