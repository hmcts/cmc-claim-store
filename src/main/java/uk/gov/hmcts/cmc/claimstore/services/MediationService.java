package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.MediationRepository;
import uk.gov.hmcts.cmc.domain.models.MediationRequest;

import java.time.LocalDate;

@Service
public class MediationService {

    private MediationRepository mediationRepository;

    @Autowired
    public MediationService(MediationRepository mediationRepository) {
        this.mediationRepository = mediationRepository; }

    public void generateMediationExtract(LocalDate mediationDate) {
        mediationRepository.getMediationClaims(mediationDate);
    }

    public void sendMediationExtract(String mediationServiceEmail) {

    }
}
