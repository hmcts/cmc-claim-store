package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.MediationRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Mediation;
import uk.gov.hmcts.cmc.domain.models.MediationRequest;
import uk.gov.hmcts.cmc.domain.models.response.Response;

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

        mediationClaims.forEach(this::getDefendantMediationDetails);
        mediationClaims.forEach(this::getClaimantMediationDetails);
    }

    private Mediation getDefendantMediationDetails(Claim mediationClaim) {
        return new Mediation(
            mediationClaim.getReferenceNumber(),
            mediationClaim.getTotalAmountTillToday(),
            2,
            mediationClaim.getResponse().flatMap(Response::getMediationContactPerson),
            mediationClaim.getResponse().flatMap(Response::getMediationPhoneNumber)
        );
    }

    private Mediation getClaimantMediationDetails(Claim mediationClaim) {
        return new Mediation(
            mediationClaim.getReferenceNumber(),
            mediationClaim.getTotalAmountTillToday(),
            1,
            mediationClaim.getResponse().flatMap(Response::getMediationContactPerson),
            mediationClaim.getResponse().flatMap(Response::getMediationPhoneNumber)
        );
    }

}
