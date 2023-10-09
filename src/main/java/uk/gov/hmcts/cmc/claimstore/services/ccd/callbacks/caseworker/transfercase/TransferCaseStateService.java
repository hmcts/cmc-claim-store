package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;

@Service
public class TransferCaseStateService {

    private final UserService userService;
    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    public TransferCaseStateService(
        UserService userService,
        CoreCaseDataService coreCaseDataService
    ) {
        this.userService = userService;
        this.coreCaseDataService = coreCaseDataService;
    }

    public void transferGivenCaseState(CaseEvent caseEvent, Long caseId) {

        CCDCase ccdCaseId = caseId != null ? CCDCase.builder()
            .id(caseId)
            .build() : null;

            coreCaseDataService.caseTransferUpdate(
                userService.authenticateAnonymousCaseWorker().getAuthorisation(),
                ccdCaseId,
                caseEvent
            );
        }
    }
