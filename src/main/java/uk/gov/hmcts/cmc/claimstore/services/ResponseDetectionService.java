package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.scheduler.services.ResponseDetection;

import java.util.List;
import java.util.Optional;

@Component
public class ResponseDetectionService implements ResponseDetection {
    private final CCDCaseApi ccdCaseApi;

    public ResponseDetectionService(CCDCaseApi ccdCaseApi){
        this.ccdCaseApi = ccdCaseApi;
    }
    @Override
    public boolean isAlreadyResponded(String caseId, String defendantId) {
        List<Claim> claims = ccdCaseApi.getByDefendantId(defendantId, "");
        Optional<Claim> claim = claims.stream().filter(c -> c.getId().equals(Long.valueOf(caseId))).findFirst();
        return claim.filter(c -> c.getResponseDeadline() != null).isPresent();
    }
}
