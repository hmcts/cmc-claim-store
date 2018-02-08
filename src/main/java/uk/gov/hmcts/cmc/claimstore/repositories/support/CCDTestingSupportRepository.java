package uk.gov.hmcts.cmc.claimstore.repositories.support;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.Optional;

@Service("testingSupportRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDTestingSupportRepository implements SupportRepository {

    private final CCDCaseApi ccdCaseApi;

    @Autowired
    public CCDTestingSupportRepository(CCDCaseApi ccdCaseApi) {
        this.ccdCaseApi = ccdCaseApi;
    }

    @Override
    public void updateResponseDeadline(Long claimId, LocalDate responseDeadline, String authorisation) {
        throw new NotImplementedException("Not implemented yet! Not sure which event we should add in CCD Case");
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        return this.ccdCaseApi.getByReferenceNumber(claimReferenceNumber, authorisation);
    }
}
