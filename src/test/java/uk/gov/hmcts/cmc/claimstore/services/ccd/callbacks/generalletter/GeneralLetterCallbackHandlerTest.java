package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.floatThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@ExtendWith(MockitoExtension.class)
class GeneralLetterCallbackHandlerTest {

    @Mock
    private GeneralLetterService generalLetterService;

    private GeneralLetterCallbackHandler handler;

    private CallbackRequest callbackRequest;

    private CallbackParams callbackParams;

    private static final String EXISTING_DATA = "existingData";
    private static final String DATA = "data";
    private CaseDetails caseDetails;
    private Map<String, Object> data;
    private static final String LETTER_CONTENT = "letterContent";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final String DRAFT_LETTER_DOC_KEY = "draftLetterDoc";
    public static final String GENERAL_LETTER_TEMPLATE_ID = "generalLetterTemplateId";

    @BeforeEach
    void setUp() {
        handler = new GeneralLetterCallbackHandler(generalLetterService, GENERAL_LETTER_TEMPLATE_ID);

        data = new HashMap<>();
        data.put(CHANGE_CONTACT_PARTY, "claimant");
        data.put(LETTER_CONTENT, "content");
        caseDetails = CaseDetails.builder()
            .data(data)
            .build();
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .build();
        callbackParams = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }

    @Test
    void shouldSendForCreateAndPreview() {
        Map<String, Object> dataMap = ImmutableMap.<String, Object>builder()
            .put(DRAFT_LETTER_DOC_KEY, EXISTING_DATA)
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(dataMap)
                .build();
        when(generalLetterService.createAndPreview(eq(caseDetails),
            eq(BEARER_TOKEN.name()),
            eq(DRAFT_LETTER_DOC_KEY),
            eq(GENERAL_LETTER_TEMPLATE_ID))).thenReturn(response);
        handler.createAndPreview(callbackParams);
        verify(generalLetterService, once()).createAndPreview(caseDetails, BEARER_TOKEN.name(),
            DRAFT_LETTER_DOC_KEY, GENERAL_LETTER_TEMPLATE_ID);
        assertThat(response.getData().get(DRAFT_LETTER_DOC_KEY)).isEqualTo(EXISTING_DATA);
    }

    @Test
    void shouldSendForPrintAndUpdateCaseDocuments() {
        Map<String, Object> dataMap = ImmutableMap.<String, Object>builder()
            .put(DATA, EXISTING_DATA)
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(dataMap)
                .build();
        when(generalLetterService.printAndUpdateCaseDocuments(eq(caseDetails),
            eq(BEARER_TOKEN.name()))).thenReturn(response);
        handler.printAndUpdateCaseDocuments(callbackParams);
        verify(generalLetterService, once()).printAndUpdateCaseDocuments(caseDetails, BEARER_TOKEN.name());
        assertThat(response.getData()).isEqualTo(dataMap);
    }
}
