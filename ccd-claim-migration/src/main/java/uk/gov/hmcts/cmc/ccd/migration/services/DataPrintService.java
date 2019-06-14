package uk.gov.hmcts.cmc.ccd.migration.services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.InterestMapper;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDEventsService;
import uk.gov.hmcts.cmc.ccd.migration.client.CaseEventDetails;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.mappers.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DataPrintService {

    private static final Logger logger = LoggerFactory.getLogger(DataPrintService.class);

    private final SearchCCDCaseService searchCCDCaseService;
    private final SearchCCDEventsService searchCCDEventsService;
    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;
    private final InterestMapper interestMapper;

    public DataPrintService(
        SearchCCDCaseService searchCCDCaseService,
        SearchCCDEventsService searchCCDEventsService,
        CaseMapper caseMapper,
        JsonMapper jsonMapper,
        InterestMapper interestMapper
    ) {
        this.searchCCDCaseService = searchCCDCaseService;
        this.searchCCDEventsService = searchCCDEventsService;
        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
        this.interestMapper = interestMapper;
    }

    public void printCaseDetails(
        Claim claim,
        User user,
        AtomicInteger updatedClaims
    ) {
        String referenceNumber = claim.getReferenceNumber();
        try {
            searchCCDCaseService.getCcdCaseByReferenceNumber(user, referenceNumber)
                .ifPresent(details -> printEvents(details, user, claim, updatedClaims));
        } catch (Exception e) {
            logger.info("Data search failed for claim for reference {} due to {}",
                referenceNumber,
                e.getMessage()
            );
        }
    }

    private void printEvents(CaseDetails details, User user, Claim claim, AtomicInteger updatedClaims) {
        CCDCase ccdCase = mapToCCDCase(details.getData(), Long.toString(details.getId()));
        if (ccdCase.getInterestDateType() == null
            && ccdCase.getInterestEndDateType() == null
            && ccdCase.getInterestClaimStartDate() == null
            && StringUtils.isBlank(ccdCase.getInterestStartDateReason())
        ) {
            updatedClaims.incrementAndGet();
            logger.info("interest before and after of {} are equal {}",
                claim.getReferenceNumber(),
                interestMapper.from(ccdCase).equals(claim.getClaimData().getInterest()));
        } else {
            return;
        }

        logger.info(claim.getReferenceNumber()
            + ","
            + claim.getClaimData().getInterest()
            + ","
            + ccdCase.getInterestReason()
            + ","
            + ccdCase.getInterestType()
            + ","
            + ccdCase.getInterestBreakDownAmount()
            + ","
            + ccdCase.getInterestBreakDownExplanation()
            + ","
            + ccdCase.getInterestSpecificDailyAmount()
            + ","
            + ccdCase.getInterestClaimStartDate()
            + ","
            + ccdCase.getInterestDateType()
            + ","
            + ccdCase.getInterestEndDateType()
            + ","
            + ccdCase.getInterestStartDateReason());

        List<CaseEventDetails> events
            = searchCCDEventsService.getCcdCaseEventsForCase(user, Long.toString(details.getId()));

        events.forEach(event -> {
            Claim result = caseMapper.from(mapToCCDCase(event.getData(), Long.toString(details.getId())));
            logger.info(result.getReferenceNumber()
                + " "
                + event.getEventName()
                + " "
                + event.getCreatedDate()
                + " "
                + result.getResponseDeadline()
            );
        });
    }

    private CCDCase mapToCCDCase(Map<String, Object> caseData, String caseId) {
        caseData.put("id", caseId);
        return jsonMapper.fromMap(caseData, CCDCase.class);
    }
}


