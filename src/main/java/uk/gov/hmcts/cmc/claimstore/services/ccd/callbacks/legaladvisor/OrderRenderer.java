package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_JUDGE_DIRECTIONS;

@Component
public class OrderRenderer {
    private final DocAssemblyService docAssemblyService;
    private final String legalAdvisorTemplateId;
    private final String judgeTemplateId;
    private final UserService userService;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;

    public OrderRenderer(
        DocAssemblyService docAssemblyService,
        UserService userService,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper,
        @Value("${doc_assembly.templateId}") String legalAdvisorTemplateId,
        @Value("${doc_assembly.judgeTemplateId}") String judgeTemplateId
    ) {
        this.docAssemblyService = docAssemblyService;
        this.legalAdvisorTemplateId = legalAdvisorTemplateId;
        this.judgeTemplateId = judgeTemplateId;
        this.userService = userService;
        this.docAssemblyTemplateBodyMapper = docAssemblyTemplateBodyMapper;
    }

    public DocAssemblyResponse renderLegalAdvisorOrder(CCDCase ccdCase, String authorisation) {
        return renderOrder(ccdCase, authorisation, legalAdvisorTemplateId);
    }

    public DocAssemblyResponse renderJudgeOrder(CCDCase ccdCase, String authorisation) {
        return renderOrder(ccdCase, authorisation, judgeTemplateId);
    }

    public DocAssemblyResponse renderOrder(CCDCase ccdCase, String authorisation) {
        ClaimState claimState = ClaimState.fromValue(ccdCase.getState());
        return claimState == READY_FOR_JUDGE_DIRECTIONS  ? renderJudgeOrder(ccdCase, authorisation)
            : renderLegalAdvisorOrder(ccdCase, authorisation);
    }

    private DocAssemblyResponse renderOrder(CCDCase ccdCase, String authorisation, String templateId) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        return docAssemblyService.renderTemplate(ccdCase,
            authorisation,
            templateId,
            docAssemblyTemplateBodyMapper.from(ccdCase, userDetails));
    }
}
