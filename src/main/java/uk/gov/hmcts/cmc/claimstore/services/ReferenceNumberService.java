package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
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
