package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

@Service
public class LetterGeneratorService {
    private final String generalLetterTemplateId;
    private final DocAssemblyService docAssemblyService;

    public LetterGeneratorService(
        @Value("${doc_assembly.contactChangeTemplateId}") String generalLetterTemplateId,
        DocAssemblyService docAssemblyService
    ) {
        this.generalLetterTemplateId = generalLetterTemplateId;
        this.docAssemblyService = docAssemblyService;
    }

    public DocAssemblyResponse createGeneralLetter(CCDCase ccdCase, String authorisation) {
        return docAssemblyService.changeContactLetter(ccdCase, authorisation, generalLetterTemplateId);
    }
}
