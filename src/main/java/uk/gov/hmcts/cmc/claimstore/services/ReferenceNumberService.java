package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;

@Service
public class ReferenceNumberService {

    private final ReferenceNumberRepository referenceNumberRepository;

    @Autowired
    public ReferenceNumberService(ReferenceNumberRepository referenceNumberRepository) {
        this.referenceNumberRepository = referenceNumberRepository;
    }

    public String getReferenceNumber(boolean claimantRepresented) {
        if (claimantRepresented) {
            return this.referenceNumberRepository.getReferenceNumberForLegal();
        } else {
            return this.referenceNumberRepository.getReferenceNumberForCitizen();
        }
    }

}
