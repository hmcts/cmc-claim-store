package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimTTL;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.services.CaseEventService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFAULT_CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MEDIATION_SUCCESSFUL;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PAPER_HAND_OFF;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SETTLED_PRE_JUDGMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.STAY_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TRANSFER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TRANSFER_TO_CCBC;

@Service
public class RetainAndDisposeService {

    private final CaseEventService caseEventService;
    private final UserService userService;
    List<CaseEvent> TTLEvents = List.of(INTERLOCUTORY_JUDGMENT, SETTLED_PRE_JUDGMENT, STAY_CLAIM, CCJ_REQUESTED,
    PAPER_HAND_OFF, TRANSFER, PROCEEDS_IN_CASEMAN, TRANSFER_TO_CCBC,
    DEFAULT_CCJ_REQUESTED, MEDIATION_SUCCESSFUL);

    Map<CaseEvent, Integer> EventMapTTL = Map.of(INTERLOCUTORY_JUDGMENT, 2281, SETTLED_PRE_JUDGMENT, 2190,
        STAY_CLAIM, 2190, CCJ_REQUESTED, 2281,
        PAPER_HAND_OFF, 2190, TRANSFER, 1095, PROCEEDS_IN_CASEMAN, 2190, TRANSFER_TO_CCBC, 2190,
        DEFAULT_CCJ_REQUESTED, 2281, MEDIATION_SUCCESSFUL, 2190);
    List<CaseEventDetail> TTLEventsOnCase;

    @Autowired
    public RetainAndDisposeService(CaseEventService caseEventService, UserService userService) {
        this.caseEventService = caseEventService;
        this.userService =  userService;
    }

    public CCDClaimTTL calculateTTL(CaseDetails caseDetails, String authorisation) {
        User user = userService.getUser(authorisation);
        String ccdCaseId = String.valueOf(caseDetails.getId());
        List <CaseEventDetail> caseEventDetails = caseEventService.getEventDetailsForCase(ccdCaseId, user);
        //TODO: Traverse through these events and calculate the appropriate TTL value.
        for (CaseEventDetail event : caseEventDetails) {
            if (event.getEventName().contains((CharSequence) TTLEvents) ) {
                TTLEventsOnCase.add(event);
            }
        }

        //TTLEventsOnCase.sort(Comparator.comparing(CaseEventDetail::getCreatedDate));
        var index = TTLEventsOnCase.size() - 1;
        LocalDate eventDate = TTLEventsOnCase.get(index).getCreatedDate().toLocalDate();
        CaseEvent eventName = CaseEvent.valueOf(TTLEventsOnCase.get(index).getEventName());
        var ttlIncrement = EventMapTTL.get(eventName);

        // TODO: calculate TTL value for the provided case based on the event history.
        return CCDClaimTTL.builder()
            .OverrideTTL(eventDate.plusDays(ttlIncrement)) // TODO: Set this value
            .build();
    }
}
