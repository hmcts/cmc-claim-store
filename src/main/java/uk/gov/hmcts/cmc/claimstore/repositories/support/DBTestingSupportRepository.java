package uk.gov.hmcts.cmc.claimstore.repositories.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.Optional;

@Service("supportRepository")
@ConditionalOnProperty(prefix = "feature_toggles", name = "ccd_enabled", havingValue = "false")
public class DBTestingSupportRepository implements SupportRepository {

    private final TestingSupportRepository testingSupportRepository;

    @Autowired
    public DBTestingSupportRepository(TestingSupportRepository testingSupportRepository) {
        this.testingSupportRepository = testingSupportRepository;
    }

    @Override
    public void updateResponseDeadline(String authorisation, Claim claim, LocalDate responseDeadline) {
        this.testingSupportRepository.updateResponseDeadline(claim.getExternalId(), responseDeadline);
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        return this.testingSupportRepository.getByClaimReferenceNumber(claimReferenceNumber);
    }

    @Override
    public void linkDefendantToClaim(Claim claim, String defendantId) {
        this.testingSupportRepository.updateDefendantId(claim.getExternalId(), defendantId);
    }

    @Override
    public Claim saveClaim(String authorisation, Claim claim) {
        return null; //todo
    }
}
