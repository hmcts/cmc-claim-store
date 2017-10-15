package uk.gov.hmcts.cmc.claimstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;

import java.time.LocalDate;

@Component
public class ClaimStore {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private JsonMapper jsonMapper;

    public long save(ClaimData claimData) {
        return save(claimData, 1L);
    }

    public long save(ClaimData claimData, long submitterId) {
        Logger logger = LoggerFactory.getLogger(getClass());

        logger.info("Saving claim: " + claimData.getExternalId());
        Long claimId = this.claimRepository.saveSubmittedByClaimant(
            this.jsonMapper.toJson(claimData),
            submitterId,
            SampleClaim.LETTER_HOLDER_ID,
            LocalDate.now(),
            LocalDate.now(),
            claimData.getExternalId().toString(),
            SampleClaim.SUBMITTER_EMAIL
        );

        logger.info("Saved claim has been given ID " + claimId + ". The following claims exists in the DB: "
            + this.claimRepository.findAll().stream().map(claim ->  claim.getId() + " - " + claim.getExternalId()));

        return claimId;
    }

    public void linkDefendant(long claimId, long defendantId) {
        claimRepository.linkDefendant(claimId, defendantId);
    }

}
