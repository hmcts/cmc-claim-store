package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.UpdateCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TEST_SUPPORT_UPDATE;

@Service
public class DataFixHandler {
    private static final Logger logger = LoggerFactory.getLogger(DataFixHandler.class);

    private final SearchCCDCaseService searchCCDCaseService;
    private final UpdateCCDCaseService updateCCDCaseService;

    public DataFixHandler(
        SearchCCDCaseService searchCCDCaseService,
        UpdateCCDCaseService updateCCDCaseService
    ) {
        this.searchCCDCaseService = searchCCDCaseService;
        this.updateCCDCaseService = updateCCDCaseService;
    }

    @LogExecutionTime
    public void fixClaim(
        AtomicInteger migratedClaims,
        AtomicInteger failedOnUpdateMigrations,
        AtomicInteger updatedClaims,
        Claim claim,
        User user
    ) {
        try {
            Optional<CaseDetails> caseDetails
                = searchCCDCaseService.getCcdIdByReferenceNumber(user, claim.getReferenceNumber());

            caseDetails.ifPresent(details -> {
                if (details.getState().equals(ClaimState.OPEN.getValue())) {
                    updateCase(user, updatedClaims, failedOnUpdateMigrations, claim, details);
                }
            });

        } catch (Exception e) {
            logger.info("Data Fix failed for claim for reference {} for the migrated count {} due to {}",
                claim.getReferenceNumber(),
                migratedClaims.get(),
                e.getMessage()
            );
        }
    }

    private void updateCase(
        User user,
        AtomicInteger updatedClaims,
        AtomicInteger failedMigrations,
        Claim claim,
        CaseDetails caseDetails
    ) {
        String referenceNumber = claim.getReferenceNumber();
        try {

            logger.info("start updating case for: {} for event: {}", referenceNumber, TEST_SUPPORT_UPDATE);

            updateCCDCaseService.updateCase(user, caseDetails.getId(), claim, TEST_SUPPORT_UPDATE);
            updatedClaims.incrementAndGet();

        } catch (Exception e) {
            logger.info("Claim update for events failed for Claim reference {} due to {}",
                referenceNumber,
                e.getMessage(),
                e
            );
            failedMigrations.incrementAndGet();
        }

    }
}
