package uk.gov.hmcts.cmc.ccd.migration.services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.CoreCaseDataService;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;
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

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.domain.models.response.DefenceType.ALREADY_PAID;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;

@Service
public class MigrationHandler {
    private static final Logger logger = LoggerFactory.getLogger(MigrationHandler.class);
    public static final String ON_HOLD_STATE = "onhold";
    public static final String OPEN_STATE = "open";

    private final CoreCaseDataService coreCaseDataService;
    private final long delayBetweenCasesLots;
    private final int casesLotsSize;

    public MigrationHandler(
        CoreCaseDataService coreCaseDataService,
        @Value("${migration.delay.between.cases.lots}") long delayBetweenCasesLots,
        @Value("${migration.cases.lots.size}") int casesLotsSize
    ) {
        this.coreCaseDataService = coreCaseDataService;
        this.delayBetweenCasesLots = delayBetweenCasesLots;
        this.casesLotsSize = casesLotsSize;
    }

    @LogExecutionTime
    public void migrateClaim(
        AtomicInteger migratedClaims,
        AtomicInteger failedMigrations,
        AtomicInteger updatedClaims,
        Claim claim,
        User user
    ) {
        try {
            delayMigrationWhenMigratedCaseLotsReachedAllowed(migratedClaims);

            CaseDetails details = createCase(user, migratedClaims, failedMigrations, claim);
            if (Optional.ofNullable(details).isPresent()) {
                updateCase(user, updatedClaims, failedMigrations, claim, details);
            }

        } catch (Exception e) {
            logger.info("failed migrating for claim for reference {} for the migrated count {} due to {}",
                claim.getReferenceNumber(),
                migratedClaims.get(),
                e.getMessage()
            );
            failedMigrations.incrementAndGet();
        }
    }

    private void delayMigrationWhenMigratedCaseLotsReachedAllowed(AtomicInteger migratedClaims) {

        int migratedClaimsCount = migratedClaims.get();

        if (casesLotsSize != 0 && migratedClaimsCount != 0 && (migratedClaimsCount % casesLotsSize) == 0) {
            try {
                logger.info("Sleeping for {}", delayBetweenCasesLots);
                Thread.sleep(delayBetweenCasesLots);
            } catch (InterruptedException e) {
                logger.info("failed sleeping between lots on count {}", migratedClaimsCount);
            }
        }
    }

    private void updateCase(
        User user,
        AtomicInteger updatedClaims,
        AtomicInteger failedMigrations,
        Claim claim,
        CaseDetails caseDetails
    ) {
        try {
            Arrays.stream(CaseEvent.values())
                .forEach(event -> {
                    if (eventNeedToBePerformedOnClaim(event, claim, caseDetails.getState())) {
                        logger.info("start migrating claim: "
                            + claim.getReferenceNumber()
                            + " for event: "
                            + event.getValue());

                        coreCaseDataService.updateCase(user, caseDetails.getId(), claim, event);
                        updatedClaims.incrementAndGet();
                    }
                });
        } catch (Exception e) {
            logger.info("Claim Migration failed for Claim reference "
                    + claim.getReferenceNumber()
                    + " due to " + e.getMessage(),
                e);
            failedMigrations.incrementAndGet();
        }
        //Enable below line for final run on prod
        //claimRepository.markAsMigrated(claim.getId());

    }

    private CaseDetails createCase(
        User user,
        AtomicInteger migratedClaims,
        AtomicInteger failedMigrations,
        Claim claim
    ) {
        CaseDetails caseDetails = null;
        try {
            logger.info("start migrating claim: "
                + claim.getReferenceNumber()
                + " for event: "
                + CaseEvent.CREATE_NEW_CASE.getValue());

            caseDetails = coreCaseDataService.createCase(user, claim, CaseEvent.CREATE_NEW_CASE);
            migratedClaims.incrementAndGet();
        } catch (Exception e) {
            logger.info("Claim Migration failed for Claim reference "
                    + claim.getReferenceNumber()
                    + " due to " + e.getMessage(),
                e);
            failedMigrations.incrementAndGet();
        }
        return caseDetails;
    }

    private boolean eventNeedToBePerformedOnClaim(CaseEvent event, Claim claim, String state) {
        if (StringUtils.isBlank(state) || !state.equals(OPEN_STATE)) {
            return false;
        }

        switch (event) {
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
                    && ((FullDefenceResponse) claim.getResponse().get()).getDefenceType() == ALREADY_PAID;
            case DIRECTIONS_QUESTIONNAIRE_DEADLINE:
                return claim.getRespondedAt() != null
                    && claim.getResponse().isPresent()
                    && claim.getResponse().get().getResponseType().equals(ResponseType.FULL_DEFENCE)
                    && claim.getResponse().get().getFreeMediation().filter(Predicate.isEqual(NO)).isPresent();
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
            case OFFER_COUNTER_SIGNED_BY_DEFENDANT:
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
                    && claim.getClaimantResponse().isPresent());
            case AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT:
                return claim.getSettlement().isPresent()
                    && (claim.getSettlement().get().getLastStatement().getType() == StatementType.COUNTERSIGNATURE)
                    && claim.getClaimantResponse().isPresent();
            case AGREEMENT_REJECTED_BY_DEFENDANT:
                return claim.getSettlement().isPresent()
                    && claim.getSettlement().get().getLastStatement().getType() == StatementType.REJECTION
                    && claim.getSettlement().get().getLastStatement().getMadeBy() == MadeBy.DEFENDANT
                    && claim.getClaimantResponse().isPresent();
            case INTERLOCUTORY_JUDGMENT:
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
