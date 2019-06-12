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

import java.util.List;
import java.util.Map;

@Service
public class DataPrintService {

    private static final Logger logger = LoggerFactory.getLogger(DataPrintService.class);

    private final SearchCCDCaseService searchCCDCaseService;
    private final SearchCCDEventsService searchCCDEventsService;
    private final SupportUpdateService supportUpdateService;
    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;
    private final InterestDateMapper interestDateMapper;

    public DataPrintService(
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

    public void printCaseDetails(
        String reference,
        User user
    ) {
        try {
            logger.info("Search case for: {}", reference);

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


