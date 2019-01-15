package uk.gov.hmcts.cmc.ccd.migration.services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.CoreCaseDataService;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.CaseEvent;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.ccd.migration.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;

@Service
public class ClaimMigrator {

    private static final Logger logger = LoggerFactory.getLogger(ClaimMigrator.class);

    private ClaimRepository claimRepository;
    private UserService userService;
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    public ClaimMigrator(
        ClaimRepository claimRepository,
        UserService userService,
        CoreCaseDataService coreCaseDataService) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.coreCaseDataService = coreCaseDataService;
    }

    public void migrate() {
        logger.info("===== MIGRATE CLAIMS TO CCD =====");

        User user = userService.authenticateSystemUpdateUser();
        List<Claim> notMigratedClaims = claimRepository.getAllNotMigratedClaims();

        logger.info("User token: " + user.getAuthorisation());

        AtomicInteger migratedClaims = new AtomicInteger(0);
        AtomicInteger updatedClaims = new AtomicInteger(0);
        AtomicInteger failedMigrations = new AtomicInteger(0);

        notMigratedClaims.sort(Comparator.comparing(Claim::getId));

        notMigratedClaims.forEach(claim -> {
            Optional<CaseDetails> caseDetails
                = coreCaseDataService.getCcdIdByReferenceNumber(user, claim.getReferenceNumber());

            if (!caseDetails.isPresent()) {
                createCase(user, migratedClaims, claim, CaseEvent.SUBMIT_PRE_PAYMENT, failedMigrations);
                updateCase(user, updatedClaims, failedMigrations, claim, null);
            } else {
                updateCase(user, updatedClaims, failedMigrations, claim, caseDetails.get());
            }

        });

        logger.info("Total Claims in database: " + notMigratedClaims.size());
        logger.info("Successfully migrated: " + migratedClaims.toString());
        logger.info("Successfully updated: " + updatedClaims.toString());
        logger.info("Failed to migrate: " + failedMigrations.toString());
    }

    private void updateCase(
        User user,
        AtomicInteger updatedClaims,
        AtomicInteger failedMigrations,
        Claim claim,
        CaseDetails caseDetails
    ) {

        CaseDetails ccdCase = Optional.ofNullable(caseDetails).orElseGet(() ->
            coreCaseDataService.getCcdIdByReferenceNumber(user, claim.getReferenceNumber()).orElse(null));

        if (ccdCase == null) {
            return;
        }

        try {
            for (CaseEvent event : CaseEvent.values()) {
                if (eventNeedToBePerformedOnClaim(event, claim, ccdCase.getState())) {
                    logger.info("\t\t start migrating claim: "
                        + claim.getReferenceNumber()
                        + " for event: "
                        + event.getValue());

                    coreCaseDataService.overwrite(user, ccdCase.getId(), claim, event);
                    logger.info("\t\t claim exists - overwrite");
                    updatedClaims.incrementAndGet();
                }
            }
//Enable below line for final run on prod
// claimRepository.markAsMigrated(claim.getId());
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            failedMigrations.incrementAndGet();
        }
    }

    private void createCase(
        User user,
        AtomicInteger migratedClaims,
        Claim claim,
        CaseEvent event,
        AtomicInteger failedMigrations
    ) {
        try {
            logger.info("\t\t start migrating claim: "
                + claim.getReferenceNumber()
                + " for event: "
                + event.getValue());

            coreCaseDataService.create(user, claim, event);
            logger.info("\t\t claim created in ccd");
            migratedClaims.incrementAndGet();
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            failedMigrations.incrementAndGet();
        }
    }

    private boolean eventNeedToBePerformedOnClaim(CaseEvent event, Claim claim, String state) {
        switch (event) {
            case SUBMIT_POST_PAYMENT:
                return (StringUtils.isBlank(state) || state.equals("onhold")) && claim.getCreatedAt() != null;
            case LINK_DEFENDANT:
                return StringUtils.isNotBlank(claim.getDefendantId());
            case MORE_TIME_REQUESTED_ONLINE:
                return claim.isMoreTimeRequested();
            case FULL_ADMISSION:
                return claim.getRespondedAt() != null
                    && claim.getResponse().isPresent()
                    && claim.getResponse().get().getResponseType() == ResponseType.FULL_ADMISSION;
            case PART_ADMISSION:
                return claim.getRespondedAt() != null
                    && claim.getResponse().isPresent()
                    && claim.getResponse().get().getResponseType() == ResponseType.PART_ADMISSION;
            case DISPUTE:
                return claim.getRespondedAt() != null
                    && claim.getResponse().isPresent()
                    && claim.getResponse().get().getResponseType() == ResponseType.FULL_DEFENCE
                    && ((FullDefenceResponse) claim.getResponse().get()).getDefenceType() == DefenceType.DISPUTE;
            case ALREADY_PAID:
                return claim.getRespondedAt() != null
                    && claim.getResponse().isPresent()
                    && claim.getResponse().get().getResponseType() == ResponseType.FULL_DEFENCE
                    && ((FullDefenceResponse) claim.getResponse().get()).getDefenceType() == DefenceType.ALREADY_PAID;
            case DIRECTIONS_QUESTIONNAIRE_DEADLINE:
                return claim.getRespondedAt() != null
                    && claim.getResponse().isPresent()
                    && claim.getResponse().get().getResponseType().equals(ResponseType.FULL_DEFENCE)
                    && claim.getResponse().get().getFreeMediation().filter(Predicate.isEqual(NO)).isPresent();
            case OFFER_MADE_BY_CLAIMANT:
                return claim.getSettlement().isPresent()
                    && claim.getSettlement().get().getLastStatementOfType(StatementType.OFFER)
                    .getMadeBy() == MadeBy.CLAIMANT;
            case OFFER_MADE_BY_DEFENDANT:
                return claim.getSettlement().isPresent()
                    && claim.getSettlement().get().getLastStatementOfType(StatementType.OFFER)
                    .getMadeBy() == MadeBy.DEFENDANT;
            case OFFER_SIGNED_BY_CLAIMANT:
                return claim.getSettlement().isPresent()
                    && ((claim.getSettlement().get().getLastStatement().getType() == StatementType.ACCEPTATION
                    && claim.getSettlement().get().getLastStatement().getMadeBy() == MadeBy.CLAIMANT)
                    || (claim.getSettlement().get().getLastStatement().getType() == StatementType.COUNTERSIGNATURE
                    && claim.getSettlement().get().getLastStatementOfType(StatementType.ACCEPTATION)
                    .getMadeBy() == MadeBy.CLAIMANT))
                    && !claim.getClaimantResponse().isPresent();
            case OFFER_SIGNED_BY_DEFENDANT:
                return claim.getSettlement().isPresent()
                    && (claim.getSettlement().get().getLastStatement().getType() == StatementType.COUNTERSIGNATURE)
                    && !claim.getClaimantResponse().isPresent();
            case OFFER_REJECTED_BY_DEFENDANT:
                return claim.getSettlement().isPresent()
                    && claim.getSettlement().get().getLastStatement().getType() == StatementType.REJECTION
                    && claim.getSettlement().get().getLastStatement().getMadeBy() == MadeBy.DEFENDANT
                    && !claim.getClaimantResponse().isPresent();
            case OFFER_REJECTED_BY_CLAIMANT:
                return claim.getSettlement().isPresent()
                    && claim.getSettlement().get().getLastStatement().getType() == StatementType.REJECTION
                    && claim.getSettlement().get().getLastStatement().getMadeBy() == MadeBy.CLAIMANT
                    && !claim.getClaimantResponse().isPresent();
            case SETTLED_PRE_JUDGMENT:
                return claim.getMoneyReceivedOn().isPresent();
            case CLAIMANT_RESPONSE_REJECTION:
                return claim.getClaimantRespondedAt().isPresent()
                    && claim.getClaimantResponse().isPresent()
                    && claim.getClaimantResponse().get().getType() == ClaimantResponseType.REJECTION;
            case CLAIMANT_RESPONSE_ACCEPTATION:
                return claim.getClaimantRespondedAt().isPresent()
                    && claim.getClaimantResponse().isPresent()
                    && claim.getClaimantResponse().get().getType() == ClaimantResponseType.ACCEPTATION;
            case DEFAULT_CCJ_REQUESTED:
                return claim.getCountyCourtJudgmentRequestedAt() != null
                    && claim.getCountyCourtJudgment() != null
                    && (claim.getCountyCourtJudgment().getCcjType() == null
                    || claim.getCountyCourtJudgment().getCcjType() == CountyCourtJudgmentType.DEFAULT);
            case CCJ_REQUESTED:
                return claim.getCountyCourtJudgmentRequestedAt() != null
                    && claim.getCountyCourtJudgment() != null
                    && claim.getCountyCourtJudgment().getCcjType() != null
                    && (claim.getCountyCourtJudgment().getCcjType() == CountyCourtJudgmentType.ADMISSIONS
                    || claim.getCountyCourtJudgment().getCcjType() == CountyCourtJudgmentType.DETERMINATION);
            case AGREEMENT_SIGNED_BY_CLAIMANT:
                return claim.getSettlement().isPresent()
                    && ((claim.getSettlement().get().getLastStatement().getType() == StatementType.ACCEPTATION
                    && claim.getSettlement().get().getLastStatement().getMadeBy() == MadeBy.CLAIMANT)
                    || (claim.getSettlement().get().getLastStatement().getType() == StatementType.COUNTERSIGNATURE
                    && claim.getSettlement().get().getLastStatementOfType(StatementType.ACCEPTATION)
                    .getMadeBy() == MadeBy.CLAIMANT)
                    && claim.getClaimantResponse().isPresent()
                );
            case AGREEMENT_SIGNED_BY_DEFENDANT:
                return claim.getSettlement().isPresent()
                    && (claim.getSettlement().get().getLastStatement().getType() == StatementType.COUNTERSIGNATURE)
                    && claim.getClaimantResponse().isPresent();
            case AGREEMENT_REJECTED_BY_DEFENDANT:
                return claim.getSettlement().isPresent()
                    && claim.getSettlement().get().getLastStatement().getType() == StatementType.REJECTION
                    && claim.getSettlement().get().getLastStatement().getMadeBy() == MadeBy.DEFENDANT
                    && claim.getClaimantResponse().isPresent();
            case INTERLOCATORY_JUDGEMENT:
                return claim.getClaimantResponse().isPresent()
                    && claim.getClaimantResponse().get().getType() == ClaimantResponseType.ACCEPTATION
                    && claim.getResponse().isPresent()
                    && !PartyUtils.isCompanyOrOrganisation(claim.getResponse().get().getDefendant())
                    && ((ResponseAcceptation) claim.getClaimantResponse().get()).getFormaliseOption().isPresent()
                    && ((ResponseAcceptation) claim.getClaimantResponse().get()).getFormaliseOption()
                    .get() == FormaliseOption.REFER_TO_JUDGE;

            case REJECT_ORGANISATION_PAYMENT_PLAN:
                return claim.getClaimantResponse().isPresent()
                    && claim.getClaimantResponse().get().getType() == ClaimantResponseType.ACCEPTATION
                    && claim.getResponse().isPresent()
                    && PartyUtils.isCompanyOrOrganisation(claim.getResponse().get().getDefendant())
                    && ((ResponseAcceptation) claim.getClaimantResponse().get()).getFormaliseOption().isPresent()
                    && ((ResponseAcceptation) claim.getClaimantResponse().get()).getFormaliseOption()
                    .get() == FormaliseOption.REFER_TO_JUDGE;
            case REFER_TO_JUDGE_BY_CLAIMANT:
                return claim.getReDeterminationRequestedAt().isPresent()
                    && claim.getReDetermination().isPresent()
                    && claim.getReDetermination().get().getPartyType() == MadeBy.CLAIMANT;
            case REFER_TO_JUDGE_BY_DEFENDANT:
                return claim.getReDeterminationRequestedAt().isPresent()
                    && claim.getReDetermination().isPresent()
                    && claim.getReDetermination().get().getPartyType() == MadeBy.DEFENDANT;
            default:
                return false;
        }
    }

}
