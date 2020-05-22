package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

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

    public TransferCaseMidProcessor(
        DocAssemblyService docAssemblyService,
        NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper,
        @Value("${doc_assembly.noticeOfTransferSentToCourtTemplateId}") String noticeOfTransferSentToCourtTemplateId,
        @Value("${doc_assembly.noticeOfTransferSentToDefendantTemplateId}")
            String noticeOfTransferSentToDefendantTemplateId,
        CaseDetailsConverter caseDetailsConverter) {
        this.docAssemblyService = docAssemblyService;
        this.noticeOfTransferLetterTemplateMapper = noticeOfTransferLetterTemplateMapper;
        this.noticeOfTransferSentToCourtTemplateId = noticeOfTransferSentToCourtTemplateId;
        this.noticeOfTransferSentToDefendantTemplateId = noticeOfTransferSentToDefendantTemplateId;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    public CallbackResponse generateNoticeOfTransferLetters(CallbackParams callbackParams) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        ImmutableMap.Builder<String, Object> data = ImmutableMap.<String, Object>builder();
        var callbackResponse = AboutToStartOrSubmitCallbackResponse.builder();

        DocAssemblyTemplateBody formPayloadForCourt =
            noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForCourt(
                ccdCase);

        CCDDocument noticeOfTransferLetterForCourt = docAssemblyService.generateLetterAsDocument(authorisation,
            formPayloadForCourt,
            noticeOfTransferSentToCourtTemplateId);

        data.put(COVER_LETTER_DOC, noticeOfTransferLetterForCourt);

        if (!isDefendantLinked(ccdCase)) {

            DocAssemblyTemplateBody formPayloadForDefendant =
                noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForDefendant(
                    ccdCase);

            CCDDocument noticeOfTransferLetterForDefendant = docAssemblyService.generateLetterAsDocument(authorisation,
                formPayloadForDefendant,
                noticeOfTransferSentToDefendantTemplateId);

            data.put(DRAFT_LETTER_DOC, noticeOfTransferLetterForDefendant);
        }

        return callbackResponse.data(data.build()).build();
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }
}
