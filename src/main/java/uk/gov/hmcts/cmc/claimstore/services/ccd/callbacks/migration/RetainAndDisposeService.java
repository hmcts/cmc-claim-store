package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.services.CaseEventService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Service
public class RetainAndDisposeService {

    private final CaseEventService caseEventService;

    @Autowired
    public RetainAndDisposeService(CaseEventService caseEventService) {
        this.caseEventService = caseEventService;
    }

    public Object calculateTTL(CaseDetails caseDetails) {
        // TODO: calculate TTL value for the provided case based on the event history.
        return null;
    }
}
