package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.documents.CCJByAdmissionOrDeterminationPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.CountyCourtJudgmentRule;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.utils.ResponseUtils;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LIFT_STAY;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCJ_REQUESTED_AFTER_SETTLEMENT_BREACH;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCJ_REQUESTED_BY_ADMISSION;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.REDETERMINATION_REQUESTED;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForReferToJudgeFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.DETERMINATION;

@Component
public class CountyCourtJudgmentService {

    private final ClaimService claimService;
    private final AuthorisationService authorisationService;
    private final EventProducer eventProducer;
    private final CountyCourtJudgmentRule countyCourtJudgmentRule;
    private final UserService userService;
    private final AppInsights appInsights;
    private final CaseRepository caseRepository;
    private final CCJByAdmissionOrDeterminationPdfService ccjByAdmissionOrDeterminationPdfService;
    private final DocumentsService documentService;
    private final boolean ctscEnabled;
    private final ClaimantResponseReceiptService claimantResponseReceiptService;
    private final LaunchDarklyClient launchDarklyClient;

    @Autowired
    public CountyCourtJudgmentService(
        ClaimService claimService,
        AuthorisationService authorisationService,
        EventProducer eventProducer,
        CountyCourtJudgmentRule countyCourtJudgmentRule,
        UserService userService,
        AppInsights appInsights,
        CaseRepository caseRepository,
        CCJByAdmissionOrDeterminationPdfService ccjByAdmissionOrDeterminationPdfService,
        DocumentsService documentService,
        @Value("${feature_toggles.ctsc_enabled}") boolean ctscEnabled,
        ClaimantResponseReceiptService claimantResponseReceiptService,
        LaunchDarklyClient launchDarklyClient

    ) {
        this.claimService = claimService;
        this.authorisationService = authorisationService;
        this.eventProducer = eventProducer;
        this.countyCourtJudgmentRule = countyCourtJudgmentRule;
        this.userService = userService;
        this.appInsights = appInsights;
        this.caseRepository = caseRepository;
        this.ccjByAdmissionOrDeterminationPdfService = ccjByAdmissionOrDeterminationPdfService;
        this.documentService = documentService;
        this.ctscEnabled = ctscEnabled;
        this.claimantResponseReceiptService = claimantResponseReceiptService;
        this.launchDarklyClient = launchDarklyClient;

    }

    public Claim save(
        CountyCourtJudgment countyCourtJudgment,
        String externalId,
        String authorisation

    ) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);

        authorisationService.assertIsSubmitterOnClaim(claim, userDetails.getId());

        countyCourtJudgmentRule.assertCountyCourtJudgmentCanBeRequested(claim, countyCourtJudgment.getCcjType());

        if (launchDarklyClient.isFeatureEnabled("breathing-space", LaunchDarklyClient.CLAIM_STORE_USER)) {
            countyCourtJudgmentRule.assertCountyCourtJudgmentCannotBeRequested(claim);
        }

        if (ClaimState.STAYED.equals(claim.getState())
            && claim.getResponse().filter(ResponseUtils::isAdmissionResponse).isPresent()
        ) {
            claim = caseRepository.saveCaseEvent(authorisation, claim, LIFT_STAY);
        }

        claimService.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);

        Claim claimWithCCJ = claimService.getClaimByExternalId(externalId, authorisation);

        Claim claimWithCCJDocument = uploadClaimantResponseDocumentToDocumentStore(claimWithCCJ,
            countyCourtJudgment, authorisation);

        eventProducer.createCountyCourtJudgmentEvent(claimWithCCJDocument, authorisation);

        AppInsightsEvent appInsightsEvent = CCJ_REQUESTED;
        if (countyCourtJudgment.getCcjType() == CountyCourtJudgmentType.ADMISSIONS) {
            appInsightsEvent = CCJ_REQUESTED_BY_ADMISSION;
        }

        if (countyCourtJudgmentRule.isCCJDueToSettlementBreach(claimWithCCJDocument)) {
            appInsightsEvent = CCJ_REQUESTED_AFTER_SETTLEMENT_BREACH;
        }

        appInsights.trackEvent(appInsightsEvent, AppInsights.REFERENCE_NUMBER, claim.getReferenceNumber());

        return claimWithCCJDocument;
    }

    public Claim reDetermination(
        ReDetermination redetermination,
        String externalId,
        String authorisation
    ) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);

        authorisationService.assertIsParticipantOnClaim(claim, userDetails.getId());
        countyCourtJudgmentRule.assertRedeterminationCanBeRequestedOnCountyCourtJudgment(claim);

        claimService.saveReDetermination(authorisation, claim, redetermination);

        Claim claimWithReDetermination = claimService.getClaimByExternalId(externalId, authorisation);

        Claim claimWithReDeterminationDoc = uploadRedeterminationDocumentToDocumentStore(claimWithReDetermination,
            redetermination, authorisation);

        eventProducer.createRedeterminationEvent(
            claimWithReDeterminationDoc,
            authorisation,
            userDetails.getFullName(),
            redetermination.getPartyType()
        );

        appInsights.trackEvent(REDETERMINATION_REQUESTED, AppInsights.REFERENCE_NUMBER, claim.getReferenceNumber());

        return claimWithReDeterminationDoc;

    }

    private Claim uploadClaimantResponseDocumentToDocumentStore(
        Claim claim,
        CountyCourtJudgment countyCourtJudgment,
        String authorisation) {
        Claim updateClaim = claim;
        if (ctscEnabled && (countyCourtJudgment.getCcjType() == ADMISSIONS
            || countyCourtJudgment.getCcjType() == DETERMINATION)) {
            PDF document = ccjByAdmissionOrDeterminationPdfService.createPdf(claim);
            updateClaim = documentService.uploadToDocumentManagement(document, authorisation, claim);
        }
        return updateClaim;
    }

    private Claim uploadRedeterminationDocumentToDocumentStore(
        Claim claim,
        ReDetermination reDetermination,
        String authorisation) {
        Claim updateClaim = claim;
        if (ctscEnabled) {
            String partyType = reDetermination.getPartyType().name().toLowerCase();
            PDF document = claimantResponseReceiptService.createPdf(claim,
                buildRequestForReferToJudgeFileBaseName(claim.getReferenceNumber(), partyType));
            updateClaim = documentService.uploadToDocumentManagement(document, authorisation, claim);
        }
        return updateClaim;
    }
}
