package uk.gov.hmcts.cmc.claimstore.repositories.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.Optional;

@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
@Service("supportRepository")
public class CCDTestingSupportRepository implements SupportRepository {

    private final CCDCaseApi ccdCaseApi;
    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    public CCDTestingSupportRepository(CCDCaseApi ccdCaseApi, CoreCaseDataService coreCaseDataService) {
        this.ccdCaseApi = ccdCaseApi;
        this.coreCaseDataService = coreCaseDataService;
    }

    @Override
    public void updateResponseDeadline(String authorisation, Claim claim, LocalDate responseDeadline) {
        this.coreCaseDataService.updateResponseDeadline(authorisation, claim, responseDeadline);
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        return this.ccdCaseApi.getByReferenceNumber(claimReferenceNumber, authorisation);
    }

    @Override
    public void linkDefendantToClaim(Claim claim, String defendantId) {
        this.ccdCaseApi.linkDefendantToCaseByCaseId(claim.getId().toString(), defendantId);
    }
}
