package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.MediationRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.MediationRequest;

import java.time.LocalDate;
import java.util.List;

@Service
public class MediationService {

    private MediationRepository mediationRepository;

    @Autowired
    public MediationService(MediationRepository mediationRepository) {
        this.mediationRepository = mediationRepository;
    }

    public void sendMediationExtract(MediationRequest mediationRequest, String authorisation) {
        generateMediationExtract(authorisation, mediationRequest.getMediationGenerateDate());
    }

    public List<Claim> generateMediationExtract(String authorisation, LocalDate mediationDate) {
        return mediationRepository.getMediationClaims(authorisation, mediationDate);
    }

}
