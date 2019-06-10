package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.InterestDateMapper;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDEventsService;
import uk.gov.hmcts.cmc.ccd.migration.client.CaseEventDetails;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.mappers.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.migration.util.CCDCaseUtil.isResponded;
import static uk.gov.hmcts.cmc.ccd.migration.util.CCDCaseUtil.isResponseDeadlineWithinDownTime;

@Service
public class DataFixService {

    private static final Logger logger = LoggerFactory.getLogger(DataFixService.class);

    private final SearchCCDCaseService searchCCDCaseService;
    private final SearchCCDEventsService searchCCDEventsService;
    private final SupportUpdateService supportUpdateService;
    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;
    private final InterestDateMapper interestDateMapper;

    public DataFixService(
        SearchCCDCaseService searchCCDCaseService,
        SearchCCDEventsService searchCCDEventsService,
        SupportUpdateService supportUpdateService,
        CaseMapper caseMapper,
        JsonMapper jsonMapper,
        InterestDateMapper interestDateMapper

    ) {
        this.searchCCDCaseService = searchCCDCaseService;
        this.searchCCDEventsService = searchCCDEventsService;
        this.supportUpdateService = supportUpdateService;
        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
        this.interestDateMapper = interestDateMapper;
    }

    public void fixClaimFromThirdLastEvent(
        AtomicInteger updatedClaims,
        AtomicInteger failedOnUpdate,
        Claim claim,
        User user
    ) {
        try {

            logger.info("Fix case for: {}", claim.getReferenceNumber());

            searchCCDCaseService.getCcdCaseByExternalId(user, claim.getExternalId())
                .ifPresent(details -> fixDataFromThirdLastEvent(user, details, updatedClaims, failedOnUpdate, claim));

        } catch (Exception e) {
            logger.info("Data Fix failed for claim for reference {} for the migrated count {} due to {}",
                claim.getReferenceNumber(),
                updatedClaims.get(),
                e.getMessage()
            );
        }
    }

    private void fixDataFromThirdLastEvent(
        User user,
        CaseDetails details,
        AtomicInteger updatedClaims,
        AtomicInteger failedOnUpdate,
        Claim claim
    ) {
        List<CaseEventDetails> events
            = searchCCDEventsService.getCcdCaseEventsForCase(user, Long.toString(details.getId()));

        CaseEventDetails eventDetails = getEventDetailsOf(3, events);
        CCDCase ccdCase = mapToCCDCase(eventDetails.getData(), Long.toString(details.getId()));

        CCDCase caseAfterInterestDatePatch = addInterestDatePatch(claim, eventDetails, ccdCase);
        CCDCase caseAfterResponseDeadlinePatch = addResponseDeadlinePatch(ccdCase, caseAfterInterestDatePatch);

        supportUpdateService.updateCase(user, updatedClaims, failedOnUpdate, caseAfterResponseDeadlinePatch);
    }

    private CCDCase addInterestDatePatch(Claim claim, CaseEventDetails eventDetails, CCDCase ccdCase) {
        CCDCase.CCDCaseBuilder builder = ccdCase.toBuilder();
        interestDateMapper.to(claim.getClaimData().getInterest().getInterestDate(), builder);
        CCDCase caseAfterInterestDatePatch = builder.build();

        logger.info("Updating from event {} created at {}",
            eventDetails.getEventName(),
            eventDetails.getCreatedDate()
        );

        logger.info("Interest start date reason after Patch {} from {}",
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
        return caseAfterInterestDatePatch;
    }

    public void fixClaimFromSecondLastEvent(
        AtomicInteger updatedClaims,
        AtomicInteger failedOnUpdate,
        Claim claim,
        User user
    ) {
        try {

            logger.info("Fix case for: {}", claim.getReferenceNumber());

            searchCCDCaseService.getCcdCaseByExternalId(user, claim.getExternalId())
                .ifPresent(details -> fixDataFromSecondLastEvent(user, details, updatedClaims, failedOnUpdate, claim));

        } catch (Exception e) {
            logger.info("Data Fix failed for claim for reference {} for the migrated count {} due to {}",
                claim.getReferenceNumber(),
                updatedClaims.get(),
                e.getMessage()
            );
        }
    }

    private void fixDataFromSecondLastEvent(
        User user,
        CaseDetails details,
        AtomicInteger updatedClaims,
        AtomicInteger failedOnUpdate,
        Claim claim
    ) {
        List<CaseEventDetails> events
            = searchCCDEventsService.getCcdCaseEventsForCase(user, Long.toString(details.getId()));

        CaseEventDetails validEventDetails = getEventDetailsOf(2, events);
        CCDCase ccdCase = mapToCCDCase(validEventDetails.getData(), Long.toString(details.getId()));

        CCDCase caseAfterInterestDatePatch = addInterestDatePatch(claim, validEventDetails, ccdCase);
        CCDCase caseAfterResponseDeadlinePatch = addResponseDeadlinePatch(ccdCase, caseAfterInterestDatePatch);
        CCDCase caseAfterPaidInFullDatePatch = updatePaidInFullDate(caseAfterResponseDeadlinePatch, events);

        supportUpdateService.updateCase(user, updatedClaims, failedOnUpdate, caseAfterPaidInFullDatePatch);
    }

    private CCDCase addResponseDeadlinePatch(CCDCase ccdCase, CCDCase caseAfterInterestDatePatch) {
        CCDCase caseAfterResponseDeadlinePatch = patchResponseDeadline(caseAfterInterestDatePatch);

        logger.info("Response dead line after Patch {} from {}",
            caseAfterResponseDeadlinePatch.getRespondents().get(0).getValue().getResponseDeadline(),
            ccdCase.getRespondents().get(0).getValue().getResponseDeadline()
        );
        return caseAfterResponseDeadlinePatch;
    }

    private CCDCase updatePaidInFullDate(CCDCase ccdCase, List<CaseEventDetails> events) {
        CaseEventDetails lastEventDetails = getEventDetailsOf(1, events);

        if (lastEventDetails.getEventName().equals("Settled")) { // Settled is display name returned by CCD API
            CCDCase eventCase = mapToCCDCase(lastEventDetails.getData(), Long.toString(ccdCase.getId()));
            Claim.ClaimBuilder claimBuilder = caseMapper.from(ccdCase).toBuilder();
            LocalDate moneyReceivedOn = caseMapper.from(eventCase).getMoneyReceivedOn().orElse(null);
            claimBuilder.moneyReceivedOn(moneyReceivedOn);

            logger.info("Paid in Full Date added as {}", moneyReceivedOn);
            return caseMapper.to(claimBuilder.build());
        } else {
            return ccdCase;
        }
    }

    private CCDCase patchResponseDeadline(CCDCase ccdCase) {
        if (isResponseDeadlineWithinDownTime(ccdCase) && !isResponded(ccdCase)) {
            Claim.ClaimBuilder claimBuilder = caseMapper.from(ccdCase).toBuilder();
            claimBuilder.responseDeadline(LocalDate.of(2019, 06, 10));
            return caseMapper.to(claimBuilder.build());
        } else {
            return ccdCase;
        }
    }

    private CaseEventDetails getEventDetailsOf(int positionFromLast, List<CaseEventDetails> events) {
        return events.stream()
            .sorted(Comparator.comparing(CaseEventDetails::getCreatedDate))
            .collect(Collectors.toList())
            .get(events.size() - positionFromLast);
    }

    private CCDCase mapToCCDCase(Map<String, Object> caseData, String caseId) {
        caseData.put("id", caseId);
        return jsonMapper.fromMap(caseData, CCDCase.class);
    }
}


