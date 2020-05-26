package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.TransferCaseMidProcessor.COVER_LETTER_DOC;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService.DRAFT_LETTER_DOC;

@ExtendWith(MockitoExtension.class)
class TransferCaseMidProcessorTest {

    private static final String COURT_LETTER_TEMPLATE_ID = "COURT_LETTER_TEMPLATE_ID";
    private static final String DEFENDANT_LETTER_TEMPLATE_ID = "DEFENDANT_LETTER_TEMPLATE_ID";
    private static final String AUTHORISATION = "Bearer: abcd";
    private static final String CASEWORKER_NAME = "Lucie Jones";
    private static final String DEFENDANT_ID = "4";

    @InjectMocks
    private TransferCaseMidProcessor transferCaseMidProcessor;

    @Mock
    private DocAssemblyService docAssemblyService;

    @Mock
    private NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private UserService userService;

    @Mock
    private CallbackRequest callbackRequest;

    @Mock
    private CCDCase ccdCase;

    @Mock
    private CCDDocument noticeOfTransferLetterForCourt;

    @Mock
    private CCDDocument noticeOfTransferLetterForDefendant;

    @BeforeEach
    public void beforeEach() {
        ReflectionTestUtils.setField(transferCaseMidProcessor, "noticeOfTransferSentToCourtTemplateId",
            COURT_LETTER_TEMPLATE_ID);
        ReflectionTestUtils.setField(transferCaseMidProcessor, "noticeOfTransferSentToDefendantTemplateId",
            DEFENDANT_LETTER_TEMPLATE_ID);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getFullName()).thenReturn(CASEWORKER_NAME);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        stubLetterForCourt();
    }

    @Test
    void shouldGenerateNoticeOfTransferLettersWhenDefendantIsLinked() {

        givenDefendantIsLinked(true);

        AboutToStartOrSubmitCallbackResponse callbackResponse = whenLettersAreGenerated();

        thenCourtLetterGenerated(callbackResponse);
        thenDefendantLetterGenerated(callbackResponse, false);
    }

    @Test
    void shouldGenerateNoticeOfTransferLettersWhenDefendantIsNotLinked() {

        givenDefendantIsLinked(false);

        AboutToStartOrSubmitCallbackResponse callbackResponse = whenLettersAreGenerated();

        thenCourtLetterGenerated(callbackResponse);
        thenDefendantLetterGenerated(callbackResponse, true);
    }

    private void givenDefendantIsLinked(boolean isLinked) {

        CCDRespondent.CCDRespondentBuilder defendantBuilder = CCDRespondent.builder();

        if (isLinked) {
            defendantBuilder.defendantId(DEFENDANT_ID);
        } else {
            stubLetterForDefendant();
        }

        List<CCDCollectionElement<CCDRespondent>> respondents
            = singletonList(CCDCollectionElement.<CCDRespondent>builder().value(
            defendantBuilder.build()).build());

        when(ccdCase.getRespondents()).thenReturn(respondents);
    }

    private void stubLetterForCourt() {

        DocAssemblyTemplateBody formPayloadForCourt = mock(DocAssemblyTemplateBody.class);
        when(noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForCourt(ccdCase, CASEWORKER_NAME))
            .thenReturn(formPayloadForCourt);
        when(docAssemblyService.generateDocument(AUTHORISATION, formPayloadForCourt, COURT_LETTER_TEMPLATE_ID))
            .thenReturn(noticeOfTransferLetterForCourt);
    }

    private void stubLetterForDefendant() {

        DocAssemblyTemplateBody formPayloadForDefendant = mock(DocAssemblyTemplateBody.class);
        when(noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForDefendant(ccdCase, CASEWORKER_NAME))
            .thenReturn(formPayloadForDefendant);
        when(docAssemblyService.generateDocument(AUTHORISATION, formPayloadForDefendant, DEFENDANT_LETTER_TEMPLATE_ID))
            .thenReturn(noticeOfTransferLetterForDefendant);
    }

    private AboutToStartOrSubmitCallbackResponse whenLettersAreGenerated() {

        CallbackParams callbackParams = CallbackParams.builder()
            .request(callbackRequest)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();

        return (AboutToStartOrSubmitCallbackResponse)
            transferCaseMidProcessor.generateNoticeOfTransferLetters(callbackParams);
    }

    private void thenDefendantLetterGenerated(AboutToStartOrSubmitCallbackResponse callbackResponse,
                                              boolean isGenerated) {

        assertEquals(isGenerated, callbackResponse.getData().containsKey(DRAFT_LETTER_DOC));

        if (isGenerated) {
            assertEquals(noticeOfTransferLetterForDefendant, callbackResponse.getData().get(DRAFT_LETTER_DOC));
        }
    }

    private void thenCourtLetterGenerated(AboutToStartOrSubmitCallbackResponse callbackResponse) {

        assertTrue(callbackResponse.getData().containsKey(COVER_LETTER_DOC));
        assertEquals(noticeOfTransferLetterForCourt, callbackResponse.getData().get(COVER_LETTER_DOC));
    }
}
