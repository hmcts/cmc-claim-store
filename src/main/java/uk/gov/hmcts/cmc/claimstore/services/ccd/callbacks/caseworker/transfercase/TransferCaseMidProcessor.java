package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService.DRAFT_LETTER_DOC;

@Service
public class TransferCaseMidProcessor {

    public static final String COVER_LETTER_DOC = "coverLetterDoc";
    private final DocAssemblyService docAssemblyService;
    private final NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;
    private final String noticeOfTransferSentToCourtTemplateId;
    private final String noticeOfTransferSentToDefendantTemplateId;
    private final CaseDetailsConverter caseDetailsConverter;
    private final UserService userService;

    public TransferCaseMidProcessor(
        DocAssemblyService docAssemblyService,
        NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper,
        @Value("${doc_assembly.noticeOfTransferSentToCourtTemplateId}") String noticeOfTransferSentToCourtTemplateId,
        @Value("${doc_assembly.noticeOfTransferSentToDefendantTemplateId}")
            String noticeOfTransferSentToDefendantTemplateId,
        CaseDetailsConverter caseDetailsConverter,
        UserService userService) {
        this.docAssemblyService = docAssemblyService;
        this.noticeOfTransferLetterTemplateMapper = noticeOfTransferLetterTemplateMapper;
        this.noticeOfTransferSentToCourtTemplateId = noticeOfTransferSentToCourtTemplateId;
        this.noticeOfTransferSentToDefendantTemplateId = noticeOfTransferSentToDefendantTemplateId;
        this.caseDetailsConverter = caseDetailsConverter;
        this.userService = userService;
    }

    public CallbackResponse generateNoticeOfTransferLetters(CallbackParams callbackParams) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        String caseworkerName = getCaseworkerName(authorisation);

        Map<String, Object> data = new HashMap<>();
        var callbackResponse = AboutToStartOrSubmitCallbackResponse.builder();

        DocAssemblyTemplateBody formPayloadForCourt =
            noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForCourt(
                ccdCase, caseworkerName);

        CCDDocument noticeOfTransferLetterForCourt = docAssemblyService.generateDocument(authorisation,
            formPayloadForCourt,
            noticeOfTransferSentToCourtTemplateId);

        data.put(COVER_LETTER_DOC, noticeOfTransferLetterForCourt);

        if (!isDefendantLinked(ccdCase)) {

            DocAssemblyTemplateBody formPayloadForDefendant =
                noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForDefendant(
                    ccdCase, caseworkerName);

            CCDDocument noticeOfTransferLetterForDefendant = docAssemblyService.generateDocument(authorisation,
                formPayloadForDefendant,
                noticeOfTransferSentToDefendantTemplateId);

            data.put(DRAFT_LETTER_DOC, noticeOfTransferLetterForDefendant);
        }

        return callbackResponse.data(data).build();
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }

    private String getCaseworkerName(String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        return userDetails.getFullName();
    }
}
