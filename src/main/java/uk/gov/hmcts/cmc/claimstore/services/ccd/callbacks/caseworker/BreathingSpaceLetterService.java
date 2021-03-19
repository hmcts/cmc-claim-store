package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class BreathingSpaceLetterService {
    private final GeneralLetterService generalLetterService;
    private final DocAssemblyService docAssemblyService;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;

    public BreathingSpaceLetterService(
        GeneralLetterService generalLetterService,
        DocAssemblyService docAssemblyService,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper
    ) {
        this.generalLetterService = generalLetterService;
        this.docAssemblyService = docAssemblyService;
        this.docAssemblyTemplateBodyMapper = docAssemblyTemplateBodyMapper;
    }

    public String createGeneralLetter(CCDCase ccdCase, String authorisation, String generalLetterTemplateId) {
        var docAssemblyResponse = docAssemblyService.renderTemplate(ccdCase, authorisation, generalLetterTemplateId,
            docAssemblyTemplateBodyMapper.breathingSpaceLetter(ccdCase));

        return docAssemblyResponse.getRenditionOutputLocation();
    }

    public CCDCase publishLetter(CCDCase ccdCase, Claim claim, String authorisation, CCDDocument letterDoc) {
        return generalLetterService.publishLetter(ccdCase.toBuilder().draftLetterDoc(letterDoc).build(),
            claim, authorisation,
            letterDoc.getDocumentFileName());

    }
}
