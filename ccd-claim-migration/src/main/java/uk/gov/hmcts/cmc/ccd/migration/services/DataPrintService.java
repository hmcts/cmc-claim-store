package uk.gov.hmcts.cmc.ccd.migration.services;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.SearchCCDEventsService;
import uk.gov.hmcts.cmc.ccd.migration.client.CaseEventDetails;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.mappers.JsonMapper;
import uk.gov.hmcts.cmc.ccd.migration.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

@Service
public class DataPrintService {

    private static final Logger logger = LoggerFactory.getLogger(DataPrintService.class);

    private final SearchCCDCaseService searchCCDCaseService;
    private final SearchCCDEventsService searchCCDEventsService;
    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;
    private final ClaimRepository claimRepository;

    public DataPrintService(
        SearchCCDCaseService searchCCDCaseService,
        SearchCCDEventsService searchCCDEventsService,
        CaseMapper caseMapper,
        JsonMapper jsonMapper,
        ClaimRepository claimRepository

    ) {
        this.searchCCDCaseService = searchCCDCaseService;
        this.searchCCDEventsService = searchCCDEventsService;
        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
        this.claimRepository = claimRepository;
    }

    public void printCaseDetails(
        String reference,
        User user
    ) {
        try {
            logger.info("Search case for: {}", reference);

            List<Claim> claims = claimRepository.getClaims(ImmutableList.of(reference));

            claims.forEach(claim -> {
                logger.info(claim.getClaimData().getInterest().toString());
                logger.info(claim.getClaimData().getAmount().toString());
            });

            searchCCDCaseService.getCcdCaseByReferenceNumber(user, reference)
                .ifPresent(details -> printEvents(details, user));

        } catch (Exception e) {
            logger.info("Data search failed for claim for reference {} due to {}",
                reference,
                e.getMessage()
            );
        }
    }

    private void printEvents(CaseDetails details, User user) {
        CCDCase ccdCase = mapToCCDCase(details.getData(), Long.toString(details.getId()));
        logger.info("ccdCase.getInterestReason " + ccdCase.getInterestReason());
        logger.info("ccdCase.getInterestSpecificDailyAmount " + ccdCase.getInterestSpecificDailyAmount());
        logger.info("ccdCase.getInterestBreakDownAmount " + ccdCase.getInterestBreakDownAmount());
        logger.info("ccdCase.getInterestBreakDownExplanation " + ccdCase.getInterestBreakDownExplanation());
        logger.info("ccdCase.getInterestType " + ccdCase.getInterestType());

        ccdCase.getAmountBreakDown().stream()
            .map(CCDCollectionElement::getValue)
            .forEach(ccdAmountRow -> logger.info(ccdAmountRow.getAmount() + " " + ccdAmountRow.getReason()));

        logger.info("ccdCase.getInterestClaimStartDate " + ccdCase.getInterestClaimStartDate());
        logger.info("ccdCase.getInterestRate " + ccdCase.getInterestRate());

        logger.info("ccdCase.getInterestDateType " + ccdCase.getInterestDateType());

        logger.info("ccdCase.getInterestEndDateType " + ccdCase.getInterestEndDateType());

        logger.info("ccdCase.getInterestStartDateReason " + ccdCase.getInterestStartDateReason());

        List<CaseEventDetails> events
            = searchCCDEventsService.getCcdCaseEventsForCase(user, Long.toString(details.getId()));

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

    private CCDCase mapToCCDCase(Map<String, Object> caseData, String caseId) {
        caseData.put("id", caseId);
        return jsonMapper.fromMap(caseData, CCDCase.class);
    }
}


