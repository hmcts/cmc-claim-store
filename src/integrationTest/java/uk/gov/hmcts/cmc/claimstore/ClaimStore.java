package uk.gov.hmcts.cmc.claimstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.OffersRepository;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class ClaimStore {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private OffersRepository offersRepository;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private ResponseDeadlineCalculator responseDeadlineCalculator;

    @Autowired
    private IssueDateCalculator issueDateCalculator;

    public Claim getClaim(long claimId) {
        return claimRepository.getById(claimId).orElseThrow(RuntimeException::new);
    }

    public Claim getClaimByExternalId(String externalId) {
        return claimRepository.getClaimByExternalId(externalId).orElseThrow(RuntimeException::new);
    }

    public Claim saveClaim(ClaimData claimData) {
        LocalDate issueDate = issueDateCalculator.calculateIssueDay(LocalDateTimeFactory.nowInLocalZone());
        LocalDate responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(issueDate);
        return saveClaim(claimData, "1", responseDeadline);
    }

    public Claim saveClaim(ClaimData claimData, String submitterId, LocalDate responseDeadline) {
        logger.debug("Saving claim: {}", claimData.getExternalId());

        Long claimId = this.claimRepository.saveSubmittedByClaimant(
            jsonMapper.toJson(claimData),
            submitterId,
            SampleClaim.LETTER_HOLDER_ID,
            LocalDate.now(),
            responseDeadline,
            claimData.getExternalId().toString(),
            SampleClaim.SUBMITTER_EMAIL
        );

        logger.debug("Saved claim has been given ID {}.", claimId);
        return getClaim(claimId);
    }

    public Claim saveResponse(Claim claim, Response response) {
        return saveResponse(claim, response, "1", SampleClaim.DEFENDANT_EMAIL);
    }

    public Claim saveResponse(Claim claim, Response response, String defendantId, String defendantEmail) {
        logger.debug("Saving response data with claim : {}", claim.getExternalId());

        this.claimRepository.saveDefendantResponse(claim.getExternalId(), defendantEmail, jsonMapper.toJson(response));

        logger.debug("Saved response data");

        return getClaimByExternalId(claim.getExternalId());
    }

    public Claim saveCountyCourtJudgement(String externalId, CountyCourtJudgment ccj) {
        logger.debug("Saving county court judgement with claim : {}", externalId);

        this.claimRepository.saveCountyCourtJudgment(
            externalId,
            jsonMapper.toJson(ccj)
        );

        logger.debug("Saved county court judgement");

        return getClaimByExternalId(externalId);
    }

    public Claim makeOffer(String externalId, Settlement settlement) {
        logger.debug("Saving offer with claim : {}", externalId);

        this.offersRepository.updateSettlement(
            externalId,
            jsonMapper.toJson(settlement)
        );

        logger.debug("Saved offer");

        return getClaimByExternalId(externalId);
    }

    public Claim acceptOffer(String externalId, Settlement settlement) {
        logger.debug("Accept offer with claim : {}", externalId);

        this.offersRepository.updateSettlement(
            externalId,
            jsonMapper.toJson(settlement)
        );

        logger.debug("Accepted offer");

        return getClaimByExternalId(externalId);
    }

    public Claim countersignAgreement(String externalId, Settlement settlement) {
        this.offersRepository.reachSettlement(
            externalId,
            jsonMapper.toJson(settlement),
            LocalDateTime.now()
        );

        logger.debug("Countersigned agreement");

        return getClaimByExternalId(externalId);
    }

    public Claim linkSealedClaimDocumentSelfPath(long claimId, String documentSelfPath) {
        this.claimRepository.linkSealedClaimDocument(claimId, documentSelfPath);

        return getClaim(claimId);
    }
}
