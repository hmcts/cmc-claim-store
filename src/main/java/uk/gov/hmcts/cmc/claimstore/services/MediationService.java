package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.MediationRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Mediation;
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

    public void generateMediationExtract(String authorisation, LocalDate mediationDate) {
        List<Claim> mediationClaims = mediationRepository.getMediationClaims(authorisation, mediationDate);

        for (int i = 0; i < mediationClaims.size(); i++ ) {


            new Mediation(
                mediationClaims.get(i).getReferenceNumber(),
                mediationClaims.get(i).getTotalAmountTillToday(),
                mediationClaims.get(i).getClaimData().getClaimant(),
                mediationClaims.get(i).getResponse().get().getMediationContactPerson(),
                mediationClaims.get(i).getResponse().get().getMediationPhoneNumber());
        }
    }

}
