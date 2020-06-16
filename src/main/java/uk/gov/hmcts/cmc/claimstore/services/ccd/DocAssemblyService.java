package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyRequest;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.domain.OutputType;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class DocAssemblyService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuthTokenGenerator authTokenGenerator;
    private final DocAssemblyClient docAssemblyClient;

    @Autowired
    public DocAssemblyService(
        AuthTokenGenerator authTokenGenerator,
        DocAssemblyClient docAssemblyClient
    ) {
        this.authTokenGenerator = authTokenGenerator;
        this.docAssemblyClient = docAssemblyClient;
    }

    public CCDDocument generateDocument(CCDCase ccdCase,
                                        String authorisation,
                                        DocAssemblyTemplateBody formPayload,
                                        String templateId) {

        var docAssemblyResponse = renderTemplate(
            ccdCase,
            authorisation,
            templateId,
            formPayload
            );

        return CCDDocument.builder()
          .documentUrl(docAssemblyResponse.getRenditionOutputLocation())
          .build();
    }

    public DocAssemblyResponse renderTemplate(CCDCase ccdCase,
                                              String authorisation,
                                              String templateId,
                                              DocAssemblyTemplateBody payload) {
        logger.info("Creating document request for template: {}, external id: {}", templateId, ccdCase.getExternalId());

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(templateId)
            .outputType(OutputType.PDF)
            .formPayload(payload)
            .build();

        logger.info("Sending document request for template: {} external id: {}", templateId, ccdCase.getExternalId());
        try {
            return docAssemblyClient.generateOrder(
                authorisation,
                authTokenGenerator.generate(),
                docAssemblyRequest
            );
        } catch (Exception e) {
            logger.error("Error while trying to generate a document for external id: {}", ccdCase.getExternalId());
            throw new DocumentGenerationFailedException(e);
        }
    }
}
