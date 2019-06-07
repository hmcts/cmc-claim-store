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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUPPORT_UPDATE;

@Service
public class DataFixService {
    private static final Logger logger = LoggerFactory.getLogger(DataFixService.class);

    private final SearchCCDCaseService searchCCDCaseService;
    private final UpdateCCDCaseService updateCCDCaseService;
    private final SearchCCDEventsService searchCCDEventsService;
    private final boolean dryRun;
    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;
    private final InterestDateMapper interestDateMapper;

    public DataFixService(
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

    public void fixClaimFromSecondLastEvent(
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

            caseDetails.ifPresent(details ->
                fixDataFromSecondLastEvent(user, details, failedOnUpdateMigrations, updatedClaims, claim));

        } catch (Exception e) {
            logger.info("Data Fix failed for claim for reference {} for the migrated count {} due to {}",
                claim.getReferenceNumber(),
                migratedClaims.get(),
                e.getMessage()
            );
        }
    }

    private void fixDataFromSecondLastEvent(
        User user,
        CaseDetails details,
        AtomicInteger failedOnUpdateMigrations,
        AtomicInteger updatedClaims,
        Claim claim
    ) {
        List<CaseEventDetails> events
            = searchCCDEventsService.getCcdCaseEventsForCase(user, Long.toString(details.getId()));

        CaseEventDetails secondLastEventDetails = getEventDetailsOf(2, events);
        CCDCase ccdCase = mapToCCDCase(secondLastEventDetails.getData(), Long.toString(details.getId()));

        CCDCase.CCDCaseBuilder builder = ccdCase.toBuilder();
        interestDateMapper.to(claim.getClaimData().getInterest().getInterestDate(), builder);
        CCDCase caseAfterInterestDatePatch = builder.build();

        logger.info("Updating from event {} created at {}",
            secondLastEventDetails.getEventName(),
            secondLastEventDetails.getCreatedDate()
        );

        logger.info("Interest start date Reason after Patch {} from {}",
            caseAfterInterestDatePatch.getInterestStartDateReason(),
            ccdCase.getInterestStartDateReason()
        );

        logger.info("Interest start date after Patch {} from {}",
            caseAfterInterestDatePatch.getInterestClaimStartDate(),
            ccdCase.getInterestClaimStartDate()
        );

        logger.info("Interest end date type after Patch {} from {}",
            caseAfterInterestDatePatch.getInterestEndDateType(),
            ccdCase.getInterestEndDateType()
        );

        logger.info("Interest date type after Patch {} from {}",
            caseAfterInterestDatePatch.getInterestDateType(),
            ccdCase.getInterestDateType()
        );

        CCDCase caseAfterResponseDeadlinePatch = patchResponseDeadline(caseAfterInterestDatePatch);

        logger.info("Response dead line after Patch {} from {}",
            caseAfterInterestDatePatch.getRespondents().get(0).getValue().getResponseDeadline(),
            ccdCase.getRespondents().get(0).getValue().getResponseDeadline()
        );

        CCDCase caseAfterPaidInFullDatePatch = updatePaidInFullDate(caseAfterResponseDeadlinePatch, events);

        updateCase(user, updatedClaims, failedOnUpdateMigrations, caseAfterPaidInFullDatePatch);
    }

    private CCDCase updatePaidInFullDate(CCDCase caseAfterResponseDeadlinePatch, List<CaseEventDetails> events) {
        CaseEventDetails lastEventDetails = getEventDetailsOf(1, events);

        if(lastEventDetails.getEventName().equals(CaseEvent.SETTLED_PRE_JUDGMENT))

        return lastEventDetails;
    }

    private CCDCase patchResponseDeadline(CCDCase ccdCase) {
        if (isReponseDeadlineWithinDownTime(ccdCase) && !isResponded(ccdCase)) {
            Claim.ClaimBuilder claimBuilder = caseMapper.from(ccdCase).toBuilder();
            claimBuilder.responseDeadline(LocalDate.of(2019, 06, 10));
            return caseMapper.to(claimBuilder.build());
        } else {
            return ccdCase;
        }
    }

    private CaseEventDetails getEventDetailsOf(int indexOfEvents, List<CaseEventDetails> events) {
        return events.stream()
            .sorted(Comparator.comparing(CaseEventDetails::getCreatedDate))
            .collect(Collectors.toList())
            .get(events.size() - indexOfEvents);
    }

    private boolean isResponded(CCDCase ccdCase) {
        return ccdCase.getRespondents()
            .stream()
            .map(CCDCollectionElement::getValue)
            .anyMatch(defendant -> defendant.getResponseSubmittedOn() != null);
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

    private boolean isReponseDeadlineWithinDownTime(CCDCase ccdCase) {
        return ccdCase.getRespondents()
            .stream()
            .map(CCDCollectionElement::getValue)
            .filter(respondent -> respondent.getResponseDeadline().isBefore(LocalDate.of(2019, 06, 05)))
            .filter(respondent -> respondent.getResponseDeadline().isAfter(LocalDate.of(2019, 05, 29)))
            .findAny()
            .isPresent();

    }
}


