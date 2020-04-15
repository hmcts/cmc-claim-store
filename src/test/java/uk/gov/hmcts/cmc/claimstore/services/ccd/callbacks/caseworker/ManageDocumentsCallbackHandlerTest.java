package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@ExtendWith(MockitoExtension.class)
class ManageDocumentsCallbackHandlerTest {

    ManageDocumentsCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ManageDocumentsCallbackHandler();
    }

    @Test
    void shouldNotReturnErrorsIfThereAreDifferences() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .data(ImmutableMap.of("different", "data"))
            .build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.MID)
            .request(request)
            .build();

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

        Assertions.assertNull(response.getErrors());
    }

    @Test
    void shouldReturnErrorsIfThereAreNoDifferences() {
        CaseDetails caseDetails = CaseDetails.builder().build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.MID)
            .request(request)
            .build();

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

        Assertions.assertNotNull(response.getErrors());
        assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.ERROR_MESSAGE));
    }
}
