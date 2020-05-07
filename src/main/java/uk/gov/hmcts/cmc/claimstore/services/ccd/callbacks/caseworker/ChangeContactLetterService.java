package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class ChangeContactLetterService {
    private final String generalLetterTemplateId;
    private GeneralLetterService generalLetterService;
    private CaseDetailsConverter caseDetailsConverter;
    private DocAssemblyService docAssemblyService;

    public ChangeContactLetterService(
        @Value("${doc_assembly.contactChangeTemplateId}") String generalLetterTemplateId,
        GeneralLetterService generalLetterService,
        CaseDetailsConverter caseDetailsConverter,
        DocAssemblyService docAssemblyService
    ) {
        this.generalLetterTemplateId = generalLetterTemplateId;
        this.generalLetterService = generalLetterService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.docAssemblyService = docAssemblyService;
    }

    public String createGeneralLetter(CCDCase ccdCase, String authorisation) {
        DocAssemblyResponse docAssemblyResponse
            = docAssemblyService.changeContactLetter(ccdCase, authorisation, generalLetterTemplateId);

        return docAssemblyResponse.getRenditionOutputLocation();
    }

    public CCDCase publishLetter(CCDCase ccdCase, Claim claim, String authorisation) {
        return generalLetterService
            .publishLetter(ccdCase, claim, authorisation, ccdCase.getDraftLetterDoc().getDocumentFileName());

    }
}
