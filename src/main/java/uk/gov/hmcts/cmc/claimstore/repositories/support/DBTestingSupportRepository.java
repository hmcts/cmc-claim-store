package uk.gov.hmcts.cmc.claimstore.repositories.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.Optional;

@Service("testingSupportRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBTestingSupportRepository implements SupportRepository {

    private final TestingSupportRepository testingSupportRepository;

    @Autowired
    public DBTestingSupportRepository(TestingSupportRepository testingSupportRepository) {
        this.testingSupportRepository = testingSupportRepository;
    }

    @Override
    public void updateResponseDeadline(Long claimId, LocalDate responseDeadline, String authorisation) {
        this.testingSupportRepository.updateResponseDeadline(claimId, responseDeadline);
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        return this.testingSupportRepository.getByClaimReferenceNumber(claimReferenceNumber);
    }
}
