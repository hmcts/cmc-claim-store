package uk.gov.hmcts.cmc.claimstore.services.search;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.LegacyCaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

import java.util.List;
import java.util.Optional;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBCaseRepository implements CaseRepository {

    private final LegacyCaseRepository legacyCaseRepository;
    private final JsonMapper jsonMapper;
    private final UserService userService;

    public DBCaseRepository(
        LegacyCaseRepository legacyCaseRepository,
        JsonMapper jsonMapper,
        UserService userService
    ) {
        this.legacyCaseRepository = legacyCaseRepository;
        this.jsonMapper = jsonMapper;
        this.userService = userService;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return legacyCaseRepository.getBySubmitterId(submitterId);
    }

    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return legacyCaseRepository.getClaimByExternalId(externalId);
    }

    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();
        return legacyCaseRepository.getByClaimReferenceAndSubmitter(claimReferenceNumber, submitterId);
    }

    @Override
    public Optional<Claim> linkDefendant(String externalId, String defendantId, String authorisation) {
        Optional<Claim> claim = legacyCaseRepository.getClaimByExternalId(externalId);
        if (claim.isPresent()) {
            legacyCaseRepository.linkDefendant(claim.orElseThrow(IllegalStateException::new).getId(), defendantId);
            claim = legacyCaseRepository.getClaimByExternalId(externalId);
        }
        return claim;

    }

    @Override
    public void saveCountyCourtJudgment(String authorisation, Claim claim, CountyCourtJudgment countyCourtJudgment) {
        final String externalId = claim.getExternalId();
        legacyCaseRepository.saveCountyCourtJudgment(externalId, jsonMapper.toJson(countyCourtJudgment));
    }
}
