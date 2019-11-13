package uk.gov.hmcts.cmc.claimstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.metadata.CaseMetadata;

import java.util.Optional;

@Service
public class PostClaimOperation {

    @Autowired
    private ClaimRepository claimRepository;

    @LogExecutionTime
    @Retryable(value = RuntimeException.class, maxAttempts = 25, backoff = @Backoff(delay = 500))
    public Claim getClaim(String externalId, String userAuthentication) throws Exception {

        retrieveCaseMetaData(externalId, userAuthentication)
            .map(CaseMetadata::getState)
            .filter(ClaimState.OPEN::equals)
            .orElseThrow(() -> new RuntimeException("Post Claim operation processes not complete"));

        return retrieveClaim(externalId, userAuthentication);
    }

    @Recover
    public Claim recover(RuntimeException e, String externalId, String userAuthentication) throws Exception {
        // return latest claim
        return retrieveClaim(externalId, userAuthentication);
    }

    public Claim retrieveClaim(String externalId, String userAuthentication) throws Exception {
        return claimRepository.getClaimByExternalId(externalId)
            .orElseThrow(() -> new NotFoundException("Claim not found for " + externalId));
    }

    private Optional<CaseMetadata> retrieveCaseMetaData(String externalId, String userAuthentication) throws Exception {
        return claimRepository.getClaimByExternalId(externalId)
            .map(CaseMetadata::fromClaim);
    }
}
