package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyRequest;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.domain.OutputType;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;

import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class DocAssemblyService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuthTokenGenerator authTokenGenerator;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private final DocAssemblyClient docAssemblyClient;
    private final UserService userService;
    private final String legalAdvisorTemplateId;
    private final String judgeTemplateId;

    @Autowired
    public DocAssemblyService(
        AuthTokenGenerator authTokenGenerator,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper,
        DocAssemblyClient docAssemblyClient,
        UserService userService,
        @Value("${doc_assembly.templateId}") String legalAdvisorTemplateId,
        @Value("${doc_assembly.judgeTemplateId}") String judgeTemplateId
    ) {
        this.authTokenGenerator = authTokenGenerator;
        this.docAssemblyTemplateBodyMapper = docAssemblyTemplateBodyMapper;
        this.docAssemblyClient = docAssemblyClient;
        this.userService = userService;
        this.legalAdvisorTemplateId = legalAdvisorTemplateId;
        this.judgeTemplateId = judgeTemplateId;
    }

    public CCDDocument generateDocument(String authorisation,
                                        DocAssemblyTemplateBody formPayload,
                                        String templateId) {

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(templateId)
            .outputType(OutputType.PDF)
            .formPayload(formPayload)
            .build();

        var docAssemblyResponse = docAssemblyClient.generateOrder(
            authorisation,
            authTokenGenerator.generate(),
            docAssemblyRequest
        );

        return CCDDocument.builder()
          .documentUrl(docAssemblyResponse.getRenditionOutputLocation())
          .build();
    }

    public DocAssemblyResponse createOrder(CCDCase ccdCase, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        logger.info("Doc assembly service: creating request for doc assembly");

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(getTemplateId(ccdCase.getState()))
            .outputType(OutputType.PDF)
            .formPayload(docAssemblyTemplateBodyMapper.from(ccdCase, userDetails))
            .build();

        logger.info("Doc assembly service: sending request to doc assembly");

        return docAssemblyClient.generateOrder(
            authorisation,
            authTokenGenerator.generate(),
            docAssemblyRequest
        );
    }

    public DocAssemblyResponse changeContactLetter(CCDCase ccdCase, String authorisation, String templateId) {
        logger.info("Doc assembly service: creating general letter request for doc assembly for external id: {}",
            ccdCase.getExternalId());

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(templateId)
            .outputType(OutputType.PDF)
            .formPayload(docAssemblyTemplateBodyMapper.changeContactBody(ccdCase))
            .build();

        logger.info("Doc assembly service: sending general letter request to doc assembly for external id: {}",
            ccdCase.getExternalId());
        try {
            return docAssemblyClient.generateOrder(
                authorisation,
                authTokenGenerator.generate(),
                docAssemblyRequest
            );
        } catch (Exception e) {
            logger.error("Error while trying to generate a general letter docAssembly for external id: {}",
                ccdCase.getExternalId());
            throw new DocumentGenerationFailedException(e);
        }
    }

    public DocAssemblyResponse createGeneralLetter(CCDCase ccdCase, String authorisation, String templateId) {
        logger.info("Doc assembly service: creating general letter request for doc assembly for external id: {}",
            ccdCase.getExternalId());

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(templateId)
            .outputType(OutputType.PDF)
            .formPayload(docAssemblyTemplateBodyMapper.generalLetterBody(ccdCase))
            .build();

        logger.info("Doc assembly service: sending general letter request to doc assembly for external id: {}",
            ccdCase.getExternalId());
        try {
            return docAssemblyClient.generateOrder(
                authorisation,
                authTokenGenerator.generate(),
                docAssemblyRequest
            );
        } catch (Exception e) {
            logger.error("Error while trying to generate a general letter docAssembly for external id: {}",
                ccdCase.getExternalId());
            throw new DocumentGenerationFailedException(e);
        }
    }

    private String getTemplateId(String state) {
        return Optional.ofNullable(state)
            .filter(input -> ClaimState.fromValue(input).equals(ClaimState.READY_FOR_JUDGE_DIRECTIONS))
            .isPresent()
            ? judgeTemplateId
            : legalAdvisorTemplateId;

    }
}
