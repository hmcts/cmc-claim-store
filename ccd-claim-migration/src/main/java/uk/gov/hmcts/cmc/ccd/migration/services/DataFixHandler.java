package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.InterestDateMapper;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDEventsService;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.UpdateCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.mappers.JsonMapper;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetails;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUPPORT_UPDATE;

@Service
public class DataFixHandler {
    private static final Logger logger = LoggerFactory.getLogger(DataFixHandler.class);

    private final SearchCCDCaseService searchCCDCaseService;
    private final UpdateCCDCaseService updateCCDCaseService;
    private final SearchCCDEventsService searchCCDEventsService;
    private final boolean dryRun;
    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;
    private final InterestDateMapper interestDateMapper;

    public DataFixHandler(
        SearchCCDCaseService searchCCDCaseService,
        UpdateCCDCaseService updateCCDCaseService,
        SearchCCDEventsService searchCCDEventsService,
        @Value("${migration.dryRun}") boolean dryRun,
        CaseMapper caseMapper,
        JsonMapper jsonMapper,
        InterestDateMapper interestDateMapper

    ) {
        this.searchCCDCaseService = searchCCDCaseService;
        this.updateCCDCaseService = updateCCDCaseService;
        this.searchCCDEventsService = searchCCDEventsService;
        this.dryRun = dryRun;
        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
        this.interestDateMapper = interestDateMapper;
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
            AtomicInteger skippedClaimsCount = new AtomicInteger();

            logger.info("fix case for: {}", claim.getReferenceNumber());

            Optional<CaseDetails> caseDetails
                = searchCCDCaseService.getCcdCaseByExternalId(user, claim.getExternalId());

            caseDetails
                .ifPresent(details -> {
                    List<CaseEventDetails> events
                        = searchCCDEventsService.getCcdCaseEventsForCase(user, Long.toString(details.getId()));

                    CaseEventDetails lastEventDetails = findLastEventDetails(events);
                    if (lastEventDetails.getEventName().equals(SUPPORT_UPDATE)) {
                        CaseEventDetails event = findLastSuccessfullEventDetails(events)
                            .orElseThrow(IllegalStateException::new);
                        CCDCase ccdCase = extractCaseFromEvent(event, Long.toString(details.getId()));
                        updateCase(user, updatedClaims, failedOnUpdateMigrations, claim, ccdCase);
                    } else {
                        logger.info("Data Fix can not be applied on this claim as already progressed." +
                                "claim reference: {} ccd id: {} last event: {} event created date: {} " +
                                " Skipped so for: {}",
                            claim.getReferenceNumber(),
                            details.getId(),
                            lastEventDetails.getEventName(),
                            lastEventDetails.getCreatedDate(),
                            skippedClaimsCount.incrementAndGet()
                        );
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

    private Optional<CaseEventDetails> findLastSuccessfullEventDetails(List<CaseEventDetails> events) {
        List<CaseEventDetails> sortedEvents = events.stream()
            .sorted(Comparator.comparing(CaseEventDetails::getCreatedDate))
            .collect(Collectors.toList());

        CaseEventDetails lastSuccessfulEvent = null;
        for (CaseEventDetails event : sortedEvents) {
            if (event.getEventName().equals(CaseEvent.SUPPORT_UPDATE.getValue())) {
                break;
            }
            lastSuccessfulEvent = event;
        }

        return Optional.ofNullable(lastSuccessfulEvent);
    }


    private CaseEventDetails findLastEventDetails(List<CaseEventDetails> events) {
        return events.stream()
            .sorted(Comparator.comparing(CaseEventDetails::getCreatedDate).reversed())
            .collect(Collectors.toList())
            .get(0);
    }

    private CCDCase applyInterestDatePatch(final CCDCase ccdCase, final Claim claim) {
        CCDCase.CCDCaseBuilder builder = ccdCase.toBuilder();
        if (claim.getClaimData().getInterest() != null) {
            interestDateMapper.to(claim.getClaimData().getInterest().getInterestDate(), builder);
        } else {
            logger.error("Claim with reference {} has no Interest object!", claim.getReferenceNumber());
        }
        return builder.build();
    }

    private void updateCase(
        User user,
        AtomicInteger updatedClaims,
        AtomicInteger failedMigrations,
        Claim claim,
        CCDCase ccdCase
    ) {
        String referenceNumber = claim.getReferenceNumber();
        CCDCase patchedCase = applyInterestDatePatch(ccdCase, claim);

        try {
            logger.info("start updating case for: {} for event: {}", referenceNumber, SUPPORT_UPDATE);
            if (!dryRun) {
                updateCCDCaseService.updateCase(
                    user,
                    patchedCase.getId(),
                    caseMapper.from(patchedCase),
                    SUPPORT_UPDATE
                );
            }
            updatedClaims.incrementAndGet();

        } catch (Exception exception) {
            logger.info("Claim update for events failed for Claim reference {} due to {}",
                referenceNumber,
                exception.getMessage(),
                exception
            );
            failedMigrations.incrementAndGet();
        }

    }

    private CCDCase extractCase(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("id", caseDetails.getId());
        return jsonMapper.fromMap(caseData, CCDCase.class);
    }

    private CCDCase extractCaseFromEvent(CaseEventDetails caseDetails, String caseId) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("id", caseId);
        return jsonMapper.fromMap(caseData, CCDCase.class);
    }
}
