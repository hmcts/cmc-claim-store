package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
public class CaseEventService {
    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private final CaseEventsApi caseEventsApi;
    private final UserService userService;

    private final AuthTokenGenerator authTokenGenerator;

    public CaseEventService(CaseEventsApi caseEventsApi, UserService userService, AuthTokenGenerator authTokenGenerator) {
        this.caseEventsApi = caseEventsApi;
        this.userService = userService;
        this.authTokenGenerator = authTokenGenerator;
    }

    public List<CaseEvent> findEventsForCase(String authorisation, String ccdCaseId) {
        User user = userService.authenticateAnonymousCaseWorker();
        List<CaseEvent> caseEventList = new ArrayList<CaseEvent>();

        List<CaseEventDetail> caseEventDetails = caseEventsApi.findEventDetailsForCase(user.getAuthorisation(),
            authTokenGenerator.generate(), user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID, ccdCaseId);

        caseEventDetails.sort(Comparator.comparing(CaseEventDetail::getCreatedDate).reversed());

        for (CaseEventDetail caseEventDetail : caseEventDetails) {
            CaseEvent event = CaseEvent.fromValue(caseEventDetail.getId());
            caseEventList.add(event);
        }

        return caseEventList;
    }
}
