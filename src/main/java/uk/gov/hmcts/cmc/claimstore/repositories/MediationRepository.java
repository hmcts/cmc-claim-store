package uk.gov.hmcts.cmc.claimstore.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class MediationRepository {

    private ClaimRepository claimRepository;
    private CaseRepository caseRepository;

    @Autowired
    public MediationRepository(ClaimRepository claimRepository, CaseRepository caseRepository) {
        this.claimRepository = claimRepository;
        this.caseRepository = caseRepository;
    }

    public void getMediationClaims(LocalDate claimant_responded_date) {
        claimRepository.getMediationClaimsForDate(claimant_responded_date);
    }
}
