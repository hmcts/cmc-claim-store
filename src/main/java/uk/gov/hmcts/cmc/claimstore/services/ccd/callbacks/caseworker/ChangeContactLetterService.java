package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class ChangeContactLetterService {
    private final String generalLetterTemplateId;
    private final GeneralLetterService generalLetterService;
    private final DocAssemblyService docAssemblyService;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private final String caseTypeId;
    private final String jurisdictionId;

    public ChangeContactLetterService(
        @Value("${doc_assembly.contactChangeTemplateId}") String generalLetterTemplateId,
        @Value("${ocmc.caseTypeId}") String caseTypeId,
        @Value("${ocmc.jurisdictionId}") String jurisdictionId,
        GeneralLetterService generalLetterService,
        DocAssemblyService docAssemblyService,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper
    ) {
        this.generalLetterTemplateId = generalLetterTemplateId;
        this.generalLetterService = generalLetterService;
        this.docAssemblyService = docAssemblyService;
        this.docAssemblyTemplateBodyMapper = docAssemblyTemplateBodyMapper;
        this.caseTypeId = caseTypeId;
        this.jurisdictionId = jurisdictionId;
    }

    public String createGeneralLetter(CCDCase ccdCase, String authorisation) {
        var docAssemblyResponse = docAssemblyService.renderTemplate(ccdCase, authorisation,
            generalLetterTemplateId, caseTypeId, jurisdictionId, docAssemblyTemplateBodyMapper.changeContactBody(ccdCase));

        return docAssemblyResponse.getRenditionOutputLocation();
    }

    public CCDCase publishLetter(CCDCase ccdCase, Claim claim, String authorisation, CCDDocument letterDoc, List<String> userList) {
        return generalLetterService.publishLetter(ccdCase.toBuilder().draftLetterDoc(letterDoc).build(),
            claim, authorisation,
            letterDoc.getDocumentFileName(), userList);

    }
}
