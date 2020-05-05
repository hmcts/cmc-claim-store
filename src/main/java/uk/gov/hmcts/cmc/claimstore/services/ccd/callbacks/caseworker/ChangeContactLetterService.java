package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

@Service
public class ChangeContactLetterService {
    private final String generalLetterTemplateId;
    private GeneralLetterService generalLetterService;
    private CaseDetailsConverter caseDetailsConverter;

    public ChangeContactLetterService(
        @Value("${doc_assembly.contactChangeTemplateId}") String generalLetterTemplateId,
        GeneralLetterService generalLetterService,
        CaseDetailsConverter caseDetailsConverter
    ) {
        this.generalLetterTemplateId = generalLetterTemplateId;
        this.generalLetterService = generalLetterService;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    public String createGeneralLetter(CCDCase ccdCase, String authorisation) {
        return generalLetterService.generateLetter(ccdCase, authorisation, generalLetterTemplateId);
    }

    public CallbackResponse printAndUpdateCaseDocuments(CCDCase ccdCase, Claim claim, String authorisation) {
        CCDCase updatedCCDCase = generalLetterService.processDocuments(ccdCase, claim, authorisation,
            ccdCase.getDraftLetterDoc().getDocumentFileName()
        );
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetailsConverter.convertToMap(updatedCCDCase))
            .build();
    }
}
