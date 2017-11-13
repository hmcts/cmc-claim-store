package uk.gov.hmcts.cmc.claimstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
public class ClaimStore {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private JsonMapper jsonMapper;

    public Claim saveClaim(ClaimData claimData) {
        return saveClaim(claimData, "1", LocalDate.now());
    }

    public Claim saveClaim(ClaimData claimData, String submitterId, LocalDate responseDeadline) {
        logger.info("Saving claim: " + claimData.getExternalId());

        Long claimId = this.claimRepository.saveSubmittedByClaimant(
            jsonMapper.toJson(claimData),
            submitterId,
            SampleClaim.LETTER_HOLDER_ID,
            LocalDate.now(),
            responseDeadline,
            claimData.getExternalId().toString(),
            SampleClaim.SUBMITTER_EMAIL
        );

        logger.info("Saved claim has been given ID " + claimId + ". The following claims exist in the DB: "
            + this.claimRepository.findAll().stream().map(claim -> claim.getId() + " - " + claim.getExternalId())
            .collect(Collectors.toList()));

        return claimRepository.getById(claimId).orElseThrow(RuntimeException::new);
    }

    public Claim saveResponse(long claimId, ResponseData responseData) {
        return saveResponse(claimId, responseData, "1", SampleClaim.DEFENDANT_EMAIL);
    }

    public Claim saveResponse(long claimId, ResponseData responseData, String defendantId, String defendantEmail) {
        logger.info("Saving response data with claim : " + claimId);

        this.claimRepository.saveDefendantResponse(
            claimId,
            defendantId,
            defendantEmail,
            jsonMapper.toJson(responseData)
        );

        logger.info("Saved response data");

        return claimRepository.getById(claimId).orElseThrow(RuntimeException::new);
    }

    public Claim saveCountyCourtJudgement(long claimId, CountyCourtJudgment ccj) {
        logger.info("Saving county court judgement with claim : " + claimId);

        this.claimRepository.saveCountyCourtJudgment(
            claimId,
            jsonMapper.toJson(ccj)
        );

        logger.info("Saved county court judgement");

        return claimRepository.getById(claimId).orElseThrow(RuntimeException::new);
    }

}
