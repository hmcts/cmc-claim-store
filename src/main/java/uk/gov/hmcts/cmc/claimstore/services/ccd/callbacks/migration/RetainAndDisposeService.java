package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.services.CaseEventService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.migration.service.DataMigrationServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.*;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MEDIATION_SUCCESSFUL;

@Service
public class RetainAndDisposeService {

    private final CaseEventService caseEventService;
    List<CaseEvent> events;
    List<CaseEvent> TTLevents = List.of(INTERLOCUTORY_JUDGMENT, SETTLED_PRE_JUDGMENT, STAY_CLAIM, CCJ_REQUESTED,
    PAPER_HAND_OFF, TRANSFER, PROCEEDS_IN_CASEMAN, TRANSFER_TO_CCBC,
    DEFAULT_CCJ_REQUESTED, MEDIATION_SUCCESSFUL);
    Map<CaseEvent, Integer> EventMapTTL = Map.of(INTERLOCUTORY_JUDGMENT, 2281, SETTLED_PRE_JUDGMENT, 2190,
        STAY_CLAIM, 2190, CCJ_REQUESTED, 2281,
        PAPER_HAND_OFF, 2190, TRANSFER, 1095, PROCEEDS_IN_CASEMAN, 2190, TRANSFER_TO_CCBC, 2190,
        DEFAULT_CCJ_REQUESTED, 2281, MEDIATION_SUCCESSFUL, 2190);
    List<CaseEvent> TTLEventsOnCase;
    CaseEvent TTLName;
    DataMigrationServiceImpl dataMigrationService;

    @Autowired
    public RetainAndDisposeService(CaseEventService caseEventService) {
        this.caseEventService = caseEventService;
    }

    public Object getEvents(String ccdCaseId, User user) {
        CaseDetails caseDetails = (CaseDetails) dataMigrationService.accepts();
        ccdCaseId = String.valueOf(caseDetails.getId());

        events = caseEventService.findEventsForCase(ccdCaseId, user);
        return events;
    }

    public Object calculateTTL(CaseDetails caseDetails) {

        for (CaseEvent i : TTLevents) {
            if (events.contains(TTLevents)) {
                TTLEventsOnCase.add(i);
            }
        }
        var index = TTLEventsOnCase.size() - 1;
        TTLName = TTLEventsOnCase.get(index);
        int TTLInteger = EventMapTTL.get(TTLName);
        // TODO: calculate TTL value for the provided case based on the event history.
        return TTLInteger;
    }
}
