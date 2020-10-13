package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRendererTest {

    public static final String DIRECTION_TYPE_BESPOKE = "BESPOKE";
    public static final String DIRECTION_TYPE_INVALID = "INVALID";

    private OrderRenderer orderRenderer;

    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private UserService userService;
    @Mock
    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private static final String LEGAL_ADVISOR_TEMPLATE_ID = "legalAdvisorTemplateId";
    private static final String JUDGE_TEMPLATE_ID = "judgeTemplateId";
    private static final String BESPOKE_TEMPLATE_ID = "bespokeTemplateId";
    private static final String AUTHORISATION = "authorisation";

    private CCDCase ccdCase;
    private DocAssemblyTemplateBody docAssemblyTemplateBody;

    @BeforeEach
    void setUp() {
        UserDetails userDetails = UserDetails.builder().build();
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        ccdCase = CCDCase.builder().previousServiceCaseReference("OCMC00001").build();
        docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        orderRenderer = new OrderRenderer(docAssemblyService,
            userService,
            docAssemblyTemplateBodyMapper,
            LEGAL_ADVISOR_TEMPLATE_ID,
            JUDGE_TEMPLATE_ID,
            BESPOKE_TEMPLATE_ID);
    }

    @Test
    void shouldRenderLegalAdvisorOrder() {
        when(docAssemblyTemplateBodyMapper.from(any(CCDCase.class), any(UserDetails.class))).thenReturn(
            docAssemblyTemplateBody);

        orderRenderer.renderLegalAdvisorOrder(ccdCase, AUTHORISATION);

        verify(docAssemblyService).renderTemplate(ccdCase, AUTHORISATION, LEGAL_ADVISOR_TEMPLATE_ID,
            docAssemblyTemplateBody, "OCMC00001-Legal-Adviser-Directions-Order");
    }

    @Test
    void shouldRenderJudgeOrder() {
        when(docAssemblyTemplateBodyMapper.from(any(CCDCase.class), any(UserDetails.class))).thenReturn(
            docAssemblyTemplateBody);

        orderRenderer.renderJudgeOrder(ccdCase, AUTHORISATION);

        verify(docAssemblyService).renderTemplate(ccdCase, AUTHORISATION, JUDGE_TEMPLATE_ID, docAssemblyTemplateBody,
            "OCMC00001-Judge-Directions-Order");
    }

    @Test
    void shouldRenderJudgeOrderWhenStateIsReadyForJudgeDirections() {
        when(docAssemblyTemplateBodyMapper.from(any(CCDCase.class), any(UserDetails.class))).thenReturn(
            docAssemblyTemplateBody);

        ccdCase = CCDCase.builder().state(ClaimState.READY_FOR_JUDGE_DIRECTIONS.getValue())
            .previousServiceCaseReference("OCMC00001").build();

        orderRenderer.renderOrder(ccdCase, AUTHORISATION);

        verify(docAssemblyService).renderTemplate(ccdCase, AUTHORISATION, JUDGE_TEMPLATE_ID, docAssemblyTemplateBody,
            "OCMC00001-Judge-Directions-Order");
    }

    @Test
    void shouldRenderLegalAdvisorOrderWhenStateIsNotReadyForJudgeDirections() {
        when(docAssemblyTemplateBodyMapper.from(any(CCDCase.class), any(UserDetails.class))).thenReturn(
            docAssemblyTemplateBody);

        ccdCase = CCDCase.builder().state(ClaimState.OPEN.getValue()).previousServiceCaseReference("OCMC00001").build();

        orderRenderer.renderOrder(ccdCase, AUTHORISATION);

        verify(docAssemblyService).renderTemplate(ccdCase, AUTHORISATION, LEGAL_ADVISOR_TEMPLATE_ID,
            docAssemblyTemplateBody, "OCMC00001-Legal-Adviser-Directions-Order");
    }

    @Test
    void shouldRenderJudgeBespokeOrder() {
        when(docAssemblyTemplateBodyMapper.mapBespokeDirectionOrder(any(CCDCase.class),
            any(UserDetails.class))).thenReturn(docAssemblyTemplateBody);
        orderRenderer.renderJudgeBespokeOrder(ccdCase, AUTHORISATION);

        verify(docAssemblyService).renderTemplate(ccdCase, AUTHORISATION, BESPOKE_TEMPLATE_ID, docAssemblyTemplateBody);
    }

    @Test
    void shouldRenderJudgeBespokeOrderWhenDirectionTypeIsBespoke() {
        when(docAssemblyTemplateBodyMapper.mapBespokeDirectionOrder(any(CCDCase.class),
            any(UserDetails.class))).thenReturn(docAssemblyTemplateBody);

        ccdCase = CCDCase.builder().directionOrderType(DIRECTION_TYPE_BESPOKE).build();

        orderRenderer.renderOrder(ccdCase, AUTHORISATION);

        verify(docAssemblyService).renderTemplate(ccdCase, AUTHORISATION, BESPOKE_TEMPLATE_ID, docAssemblyTemplateBody);
    }

    @Test
    void shouldNotRenderJudgeBespokeOrderWhenDirectionTypeIsInvalid() {
        when(docAssemblyTemplateBodyMapper.from(any(CCDCase.class), any(UserDetails.class))).thenReturn(
            docAssemblyTemplateBody);

        ccdCase = CCDCase.builder().directionOrderType(DIRECTION_TYPE_INVALID)
            .state(ClaimState.READY_FOR_JUDGE_DIRECTIONS.getValue()).build();

        orderRenderer.renderOrder(ccdCase, AUTHORISATION);

        verify(docAssemblyService).renderTemplate(ccdCase, AUTHORISATION, JUDGE_TEMPLATE_ID, docAssemblyTemplateBody);
    }

}
