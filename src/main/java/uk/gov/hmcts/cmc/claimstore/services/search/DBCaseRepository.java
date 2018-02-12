package uk.gov.hmcts.cmc.claimstore.services.search;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.Response;

import java.util.List;
import java.util.Optional;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBCaseRepository implements CaseRepository {

    private final ClaimRepository claimRepository;
    private final JsonMapper jsonMapper;
    private final UserService userService;

    public DBCaseRepository(
        ClaimRepository claimRepository,
        JsonMapper jsonMapper,
        UserService userService
    ) {
        this.claimRepository = claimRepository;
        this.jsonMapper = jsonMapper;
        this.userService = userService;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return claimRepository.getBySubmitterId(submitterId);
    }

    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return claimRepository.getClaimByExternalId(externalId);
    }

    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();
        return claimRepository.getByClaimReferenceAndSubmitter(claimReferenceNumber, submitterId);
    }

    @Override
    public Claim linkDefendantV1(String externalId, String defendantId, String authorisation) {
        String notFoundErrorMessage = "Claim not found by external id: " + externalId;

        Claim claim = claimRepository.getClaimByExternalId(externalId)
            .orElseThrow(() -> new NotFoundException(notFoundErrorMessage));
        claimRepository.linkDefendant(claim.getId(), defendantId);

        claim = claimRepository.getClaimByExternalId(externalId)
            .orElseThrow(() -> new NotFoundException(notFoundErrorMessage));
        return claim;

    }

    @Override
    public void linkDefendantV2(String authorisation) {
        throw new NotImplementedException("Will not be implemented for DB");
    }

    @Override
    public void saveCountyCourtJudgment(String authorisation, Claim claim, CountyCourtJudgment countyCourtJudgment) {
        final String externalId = claim.getExternalId();
        claimRepository.saveCountyCourtJudgment(externalId, jsonMapper.toJson(countyCourtJudgment));
    }

    @Override
    public void saveDefendantResponse(
        Claim claim,
        String defendantId,
        String defendantEmail,
        Response response,
        String authorization
    ) {
        String defendantResponse = jsonMapper.toJson(response);
        claimRepository.saveDefendantResponse(claim.getExternalId(), defendantId, defendantEmail, defendantResponse);
    }

    @Override
    public List<Claim> getByDefendantId(String id, String authorisation) {
        return claimRepository.getByDefendantId(id);
    }

    @Override
    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        return claimRepository.getByLetterHolderId(id);
    }
}
