package uk.gov.hmcts.cmc.claimstore.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.List;

@Repository
public class MediationRepository {

    private CaseRepository caseRepository;

    @Autowired
    public MediationRepository(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public List<Claim> getMediationClaims(String authorisation, LocalDate claimant_responded_date) {
        return caseRepository.getMediationClaims(authorisation, claimant_responded_date);
    }
}
