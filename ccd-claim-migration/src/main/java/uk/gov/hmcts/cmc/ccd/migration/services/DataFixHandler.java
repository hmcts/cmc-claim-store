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
import uk.gov.hmcts.cmc.ccd.migration.client.CaseEventDetails;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.mappers.JsonMapper;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDateTime;
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
        User user,
        AtomicInteger skippedClaimsCount
    ) {
        try {

            logger.info("fix case for: {}", claim.getReferenceNumber());

            Optional<CaseDetails> caseDetails
                = searchCCDCaseService.getCcdCaseByExternalId(user, claim.getExternalId());

            caseDetails
                .ifPresent(details -> {
                    List<CaseEventDetails> events
                        = searchCCDEventsService.getCcdCaseEventsForCase(user, Long.toString(details.getId()));

                    CaseEventDetails lastEventDetails = findLastEventDetails(events);
                    CCDCase ccdCase = extractCaseFromEvent(lastEventDetails, Long.toString(details.getId()));
                    boolean hasProgressed = listEventsCreatedBetweenMigrationAndDataPatch(events).size() > 1;
                    if (lastEventDetails.getCreatedDate().isBefore(details.getLastModified())) {
                        logMessageForCsv("A", hasProgressed, ccdCase, details, lastEventDetails);
                    } else {
                        logMessageForCsv("B", hasProgressed, ccdCase, details, lastEventDetails);
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

    private void logMessageForCsv(
        String bucketType,
        Boolean hasProgressed,
        CCDCase ccdCase,
        CaseDetails details,
        CaseEventDetails lastEventDetails
    ) {
        logger.info(new StringBuilder("CSV")
            .append(",")
            .append(bucketType)
            .append(",")
            .append(ccdCase.getPreviousServiceCaseReference())
            .append(",")
            .append(ccdCase.getId())
            .append(",")
            .append(details.getLastModified())
            .append(",")
            .append(lastEventDetails.getEventName())
            .append(",")
            .append(lastEventDetails.getCreatedDate())
            .append(",")
            .append(lastEventDetails.getUserFirstName())
            .append(" ")
            .append(lastEventDetails.getUserLastName())
            .append(",")
            .append(hasProgressed)
            .toString()
        );
    }

    private List<CaseEventDetails> listEventsCreatedBetweenMigrationAndDataPatch(List<CaseEventDetails> events) {
        return events.stream()
            .filter(event -> event.getCreatedDate()
                .isBefore(LocalDateTime.of(2019, 06, 03, 19, 03, 00)))
            .filter(event -> event.getCreatedDate()
                .isAfter(LocalDateTime.of(2019, 05, 29, 23, 00, 00)))
            .collect(Collectors.toList());
    }

    private Optional<CaseEventDetails> findLastSuccesdfullEventDetails(List<CaseEventDetails> events) {
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
            .sorted(Comparator.comparing(CaseEventDetails::getCreatedDate))
            .collect(Collectors.toList())
            .get(events.size() - 1);
    }

    private CCDCase applyInterestDatePatch(final CCDCase ccdCase, final Claim claim) {
        CCDCase.CCDCaseBuilder builder = ccdCase.toBuilder();
        interestDateMapper.to(claim.getClaimData().getInterest().getInterestDate(), builder);

        CCDCase aCase = builder.build();

        logger.info("updated case interest end date type from {} to {} for ref no {}",
            ccdCase.getInterestEndDateType(),
            aCase.getInterestEndDateType(),
            claim.getReferenceNumber());
        logger.info("updated case interest date type from {} to {} for ref no {}",
            ccdCase.getInterestDateType(),
            aCase.getInterestDateType(),
            claim.getReferenceNumber());
        return aCase;
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
            logger.info("start updating case for: {} for event: {} counter: {}", referenceNumber, SUPPORT_UPDATE,
                updatedClaims.toString());
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
