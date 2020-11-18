package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleHwfClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

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
@DisplayName("HWF NO Remission Rejected Callback Handler")
class HWFNoRemissionCallbackHandlerTest {

    private static final String AUTHORISATION = "Bearer: aaaa";
    private HWFNoRemissionCallbackHandler handler;
    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    @Mock
    private DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private DefendantResponseNotificationService defendantResponseNotificationService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        handler = new HWFNoRemissionCallbackHandler(caseDetailsConverter, deadlineCalculator, caseMapper,
            eventProducer, userService);
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(CaseEvent.HWF_NO_REMISSION.getValue())
            .build();
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    @Test
    void shouldUpdateInfo() {
        Claim claim = SampleClaim.getClaimWithFullAdmission();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        Map<String, Object> mappedCaseData = new HashMap<>();
        mappedCaseData.put("helpWithFeesNumber", "139999");
        mappedCaseData.put("hwfFeeDetailsSummary", "NOT_QUALIFY_FEE_ASSISTANCE");
        mappedCaseData.put("hwfMandatoryDetails", "Details");
        when(caseDetailsConverter.convertToMap(caseMapper.to(claim))).thenReturn(mappedCaseData);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        Map<String, Object> data = response.getData();

        assertThat(data).containsEntry("helpWithFeesNumber", "139999")
            .containsEntry("hwfFeeDetailsSummary", "NOT_QUALIFY_FEE_ASSISTANCE")
            .containsEntry("hwfMandatoryDetails", "Details");

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
