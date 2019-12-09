package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyRequest;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.domain.OutputType;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class DocAssemblyService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuthTokenGenerator authTokenGenerator;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private final DocAssemblyClient     docAssemblyClient;
    private final UserService userService;
    private final String templateId;

    @Autowired
    public DocAssemblyService(
        AuthTokenGenerator authTokenGenerator,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper,
        DocAssemblyClient docAssemblyClient,
        UserService userService,
        @Value("${doc_assembly.templateId}") String templateId
    ) {
        this.authTokenGenerator = authTokenGenerator;
        this.docAssemblyTemplateBodyMapper = docAssemblyTemplateBodyMapper;
        this.docAssemblyClient = docAssemblyClient;
        this.userService = userService;
        this.templateId = templateId;
    }

    public DocAssemblyResponse createOrder(CCDCase ccdCase, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        logger.info("Doc assembly service: creating request for doc assembly");

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(templateId)
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
}
