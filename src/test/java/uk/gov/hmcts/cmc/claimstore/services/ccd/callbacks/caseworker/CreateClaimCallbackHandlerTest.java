package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.claimstore.constants.ResponseConstants.CREATE_CLAIM_DISABLED;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getLegalDataWithReps;

@RunWith(MockitoJUnitRunner.class)
public class CreateClaimCallbackHandlerTest {

    private static final String BEARER_TOKEN = "Bearer let me in";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseMapper caseMapper;
    private CallbackRequest callbackRequest;
    private CreateClaimCallbackHandler createClaimCallbackHandler;

    private final CaseDetails caseDetails = CaseDetails.builder().id(3L).data(Collections.emptyMap()).build();

    @Before
    public void setUp() {
        createClaimCallbackHandler = new CreateClaimCallbackHandler(
            caseDetailsConverter,
            caseMapper,
            true
        );

        callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CASE.getValue())
            .caseDetails(caseDetails)
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(getLegalDataWithReps());
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponse() {

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            createClaimCallbackHandler.handle(callbackParams);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getWarnings()).isNull();
    }

    @Test
    public void shouldReturnErrorWhenFeatureCreateClaimIsDisabled() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        createClaimCallbackHandler = new CreateClaimCallbackHandler(
            caseDetailsConverter,
            caseMapper,
            false
        );
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) createClaimCallbackHandler.handle(callbackParams);
        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().get(0)).isEqualTo(CREATE_CLAIM_DISABLED);
    }
}
