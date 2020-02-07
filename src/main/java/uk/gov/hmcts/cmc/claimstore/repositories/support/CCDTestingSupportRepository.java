package uk.gov.hmcts.cmc.claimstore.repositories.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.Optional;

@Service("supportRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDTestingSupportRepository implements SupportRepository {

    private final UserService userService;
    private final CCDCaseApi ccdCaseApi;
    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    public CCDTestingSupportRepository(
        UserService userService,
        CCDCaseApi ccdCaseApi,
        CoreCaseDataService coreCaseDataService
    ) {
        this.userService = userService;
        this.ccdCaseApi = ccdCaseApi;
        this.coreCaseDataService = coreCaseDataService;
    }

    @Override
    public void updateResponseDeadline(String authorisation, Claim claim, LocalDate responseDeadline) {
        this.coreCaseDataService.updateResponseDeadline(authorisation, claim.getId(), responseDeadline);
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        if (authorisation == null) {
            User user = userService.authenticateAnonymousCaseWorker();
            return this.ccdCaseApi.getByReferenceNumber(claimReferenceNumber, user.getAuthorisation());
        }
        return this.ccdCaseApi.getByReferenceNumber(claimReferenceNumber, authorisation);
    }

    @Override
    public void linkDefendantToClaim(Claim claim, String defendantId, String defendantEmail) {
        this.ccdCaseApi.linkDefendant(claim.getId().toString(), defendantId, defendantEmail);
    }
}
