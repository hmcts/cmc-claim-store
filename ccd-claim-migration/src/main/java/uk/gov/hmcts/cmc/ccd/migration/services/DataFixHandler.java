package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.InterestDateMapper;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDEventsService;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.UpdateCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.client.CaseEventDetails;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.mappers.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
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

    public void fixClaim(
        AtomicInteger migratedClaims,
        AtomicInteger failedOnUpdateMigrations,
        AtomicInteger updatedClaims,
        Claim claim,
        User user
    ) {
        try {

            logger.info("fix case for: {}", claim.getReferenceNumber());

            Optional<CaseDetails> caseDetails
                = searchCCDCaseService.getCcdCaseByExternalId(user, claim.getExternalId());

            caseDetails
                .ifPresent(details -> {
                    logger.info("Last modified date of claim "
                        + claim.getReferenceNumber()
                        + " "
                        + details.getLastModified());
                    CCDCase ccdCase = mapToCCDCase(details.getData(), Long.toString(details.getId()));
                    // fixResponseDeadline(failedOnUpdateMigrations, updatedClaims, claim, user, ccdCase);
                    // fixDataIssueWithPatch(user, details);
                    fixDataFromLastButOneEvent(user, details, failedOnUpdateMigrations, updatedClaims);
                });

        } catch (Exception e) {
            logger.info("Data Fix failed for claim for reference {} for the migrated count {} due to {}",
                claim.getReferenceNumber(),
                migratedClaims.get(),
                e.getMessage()
            );
        }
    }

    private void fixDataFromLastButOneEvent(
        User user,
        CaseDetails details,
        AtomicInteger failedOnUpdateMigrations,
        AtomicInteger updatedClaims
    ) {
        List<CaseEventDetails> events
            = searchCCDEventsService.getCcdCaseEventsForCase(user, Long.toString(details.getId()));
        //  printEvents(details, events);
        CaseEventDetails lastEventDetails = findLastButOneEventDetails(events);
        CCDCase ccdCase = mapToCCDCase(lastEventDetails.getData(), Long.toString(details.getId()));

        updateCase(user, updatedClaims, failedOnUpdateMigrations, ccdCase);
    }

    private void printEvents(CaseDetails details, List<CaseEventDetails> events) {
        events.forEach(event -> {
            Claim claim = caseMapper.from(mapToCCDCase(event.getData(), Long.toString(details.getId())));
            logger.info(claim.getReferenceNumber()
                + " "
                + event.getEventName()
                + " "
                + event.getCreatedDate()
                + " "
                + claim.getResponseDeadline()
            );
        });
    }

    private CaseEventDetails findLastButOneEventDetails(List<CaseEventDetails> events) {
        return events.get(events.size() - 2);
    }

    private void fixResponseDeadline(
        AtomicInteger failedOnUpdateMigrations,
        AtomicInteger updatedClaims,
        Claim claim,
        User user,
        CCDCase ccdCase
    ) {
        if (!claim.getClaimData().isClaimantRepresented()
            && isReponsedDeadlineWithinDownTime(ccdCase)
            && !isResponded(ccdCase)
        ) {
            Claim.ClaimBuilder claimBuilder = caseMapper.from(ccdCase).toBuilder();
            claimBuilder.responseDeadline(LocalDate.of(2019, 6, 10));
            CCDCase updatedCase = caseMapper.to(claimBuilder.build());
            updateCase(user, updatedClaims, failedOnUpdateMigrations, updatedCase);
        }
    }

    private boolean isResponded(CCDCase ccdCase) {
        return ccdCase.getRespondents()
            .stream()
            .map(CCDCollectionElement::getValue)
            .anyMatch(defendant -> defendant.getResponseSubmittedOn() != null);
    }

    private void fixDataIssueWithPatch(User user, CaseDetails details) {
        List<CaseEventDetails> events
            = searchCCDEventsService.getCcdCaseEventsForCase(user, Long.toString(details.getId()));

        CaseEventDetails lastEventDetails = findLastEventDetails(events);
        CCDCase ccdCase = mapToCCDCase(lastEventDetails.getData(), Long.toString(details.getId()));
        boolean hasProgressed = listEventsCreatedBetweenMigrationAndDataPatch(events).size() > 1;
        if (lastEventDetails.getCreatedDate().isBefore(details.getLastModified())) {
            logMessageForCsv("A", hasProgressed, ccdCase, details, lastEventDetails);
        } else {
            logMessageForCsv("B", hasProgressed, ccdCase, details, lastEventDetails);
        }
    }

    private void logMessageForCsv(
        String bucketType,
        Boolean hasProgressed,
        CCDCase ccdCase,
        CaseDetails details,
        CaseEventDetails lastEventDetails
    ) {
        logger.info("CSV" +
            "," +
            bucketType +
            "," +
            ccdCase.getPreviousServiceCaseReference() +
            "," +
            ccdCase.getId() +
            "," +
            details.getLastModified() +
            "," +
            lastEventDetails.getEventName() +
            "," +
            lastEventDetails.getCreatedDate() +
            "," +
            lastEventDetails.getUserFirstName() +
            " " +
            lastEventDetails.getUserLastName() +
            "," +
            hasProgressed
        );
    }

    private List<CaseEventDetails> listEventsCreatedBetweenMigrationAndDataPatch(List<CaseEventDetails> events) {
        return events.stream()
            .filter(event -> event.getCreatedDate()
                .isBefore(LocalDateTime.of(2019, 6, 3, 19, 3, 0)))
            .filter(event -> event.getCreatedDate()
                .isAfter(LocalDateTime.of(2019, 5, 29, 23, 0, 0)))
            .collect(Collectors.toList());
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
            .sorted(Comparator.comparing(CaseEventDetails::getCreatedDate))
            .collect(Collectors.toList())
            .get(events.size() - 1);
    }

    private CCDCase applyInterestDatePatch(final CCDCase ccdCase, final Claim claim) {
        CCDCase.CCDCaseBuilder builder = ccdCase.toBuilder();
        interestDateMapper.to(claim.getClaimData().getInterest().getInterestDate(), builder);

        CCDCase updatedCase = builder.build();

        logger.info("updated case interest end date type from {} to {} for ref no {}",
            ccdCase.getInterestEndDateType(),
            updatedCase.getInterestEndDateType(),
            claim.getReferenceNumber());
        logger.info("updated case interest date type from {} to {} for ref no {}",
            ccdCase.getInterestDateType(),
            updatedCase.getInterestDateType(),
            claim.getReferenceNumber());
        return updatedCase;
    }

    private void updateCase(
        User user,
        AtomicInteger updatedClaims,
        AtomicInteger failedMigrations,
        CCDCase ccdCase
    ) {
        String caseReference = ccdCase.getPreviousServiceCaseReference();
        try {
            logger.info("start updating case for: {} for event: {} counter: {}",
                caseReference,
                SUPPORT_UPDATE,
                updatedClaims.toString());
            if (!dryRun) {
                updateCCDCaseService.updateCase(
                    user,
                    ccdCase.getId(),
                    caseMapper.from(ccdCase),
                    SUPPORT_UPDATE
                );
            }
            updatedClaims.incrementAndGet();

        } catch (Exception exception) {
            logger.info("Claim update for events failed for Claim reference {} due to {}",
                caseReference,
                exception.getMessage(),
                exception
            );
            failedMigrations.incrementAndGet();
        }

    }

    private CCDCase mapToCCDCase(Map<String, Object> caseData, String caseId) {
        caseData.put("id", caseId);
        return jsonMapper.fromMap(caseData, CCDCase.class);
    }

    private boolean isReponsedDeadlineWithinDownTime(CCDCase ccdCase) {
        return ccdCase.getRespondents()
            .stream()
            .map(CCDCollectionElement::getValue)
            .filter(respondent -> respondent.getResponseDeadline().isBefore(LocalDate.of(2019, 6, 5)))
            .anyMatch(respondent -> respondent.getResponseDeadline().isAfter(LocalDate.of(2019, 5, 29)));

    }
}


