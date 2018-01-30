package uk.gov.hmcts.cmc.claimstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.LegacyCaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.OffersRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
public class ClaimStore {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private LegacyCaseRepository legacyCaseRepository;

    @Autowired
    private OffersRepository offersRepository;

    @Autowired
    private JsonMapper jsonMapper;

    public Claim getClaim(long claimId) {
        return claimRepository.getById(claimId).orElseThrow(RuntimeException::new);
    }

    public Claim getClaimByExternalId(String externalId) {
        return legacyCaseRepository.getClaimByExternalId(externalId).orElseThrow(RuntimeException::new);
    }

    public Claim saveClaim(ClaimData claimData) {
        return saveClaim(claimData, "1", LocalDate.now());
    }

    public Claim saveClaim(ClaimData claimData, String submitterId, LocalDate responseDeadline) {
        logger.info(String.format("Saving claim: %s", claimData.getExternalId()));

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

        return getClaim(claimId);
    }

    public Claim saveResponse(long claimId, Response response) {
        return saveResponse(claimId, response, "1", SampleClaim.DEFENDANT_EMAIL);
    }

    public Claim saveResponse(long claimId, Response response, String defendantId, String defendantEmail) {
        logger.info(String.format("Saving response data with claim : %d", claimId));

        this.claimRepository.saveDefendantResponse(
            claimId,
            defendantId,
            defendantEmail,
            jsonMapper.toJson(response)
        );

        logger.info("Saved response data");

        return getClaim(claimId);
    }

    public Claim saveCountyCourtJudgement(String externalId, CountyCourtJudgment ccj) {
        logger.info(String.format("Saving county court judgement with claim : %s", externalId));

        this.legacyCaseRepository.saveCountyCourtJudgment(
            externalId,
            jsonMapper.toJson(ccj)
        );

        logger.info("Saved county court judgement");

        return getClaimByExternalId(externalId);
    }

    public Claim makeOffer(long claimId, Settlement settlement) {
        logger.info(String.format("Saving offer with claim : %d", claimId));

        this.offersRepository.updateSettlement(
            claimId,
            jsonMapper.toJson(settlement)
        );

        logger.info("Saved offer");

        return getClaim(claimId);
    }

    public Claim acceptOffer(long claimId, Settlement settlement) {
        this.offersRepository.acceptOffer(
            claimId,
            jsonMapper.toJson(settlement),
            LocalDateTime.now()
        );

        logger.info("Accepted offer");

        return getClaim(claimId);
    }

    public Claim linkSealedClaimDocumentSelfPath(long claimId, String documentSelfPath) {
        this.claimRepository.linkSealedClaimDocument(claimId, documentSelfPath);

        return getClaim(claimId);
    }
}
