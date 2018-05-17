package uk.gov.hmcts.cmc.claimstore.repositories;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBCaseRepository implements CaseRepository {

    private final ClaimRepository claimRepository;
    private final OffersRepository offersRepository;
    private final JsonMapper jsonMapper;
    private final UserService userService;

    public DBCaseRepository(
        ClaimRepository claimRepository,
        OffersRepository offersRepository,
        JsonMapper jsonMapper,
        UserService userService
    ) {
        this.claimRepository = claimRepository;
        this.offersRepository = offersRepository;
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
    public void linkDefendant(String authorisation) {
        User defendantUser = userService.getUser(authorisation);
        String defendantId = defendantUser.getUserDetails().getId();

        defendantUser.getUserDetails().getRoles().stream()
            .filter(this::isLetterHolderRole)
            .map(this::extractLetterHolderId)
            .forEach(letterHolderId -> claimRepository.linkDefendant(letterHolderId, defendantId));
    }

    private String extractLetterHolderId(String role) {
        return StringUtils.remove(role, "letter-");
    }

    private boolean isLetterHolderRole(String role) {
        Objects.requireNonNull(role);
        return role.startsWith("letter")
            && !"letter-holder".equals(role)
            && !role.endsWith("loa1");
    }

    @Override
    public void saveCountyCourtJudgment(String authorisation, Claim claim, CountyCourtJudgment countyCourtJudgment) {
        final String externalId = claim.getExternalId();
        claimRepository.saveCountyCourtJudgment(externalId, jsonMapper.toJson(countyCourtJudgment));
    }

    @Override
    public void saveDefendantResponse(Claim claim, String defendantEmail, Response response, String authorization) {
        String defendantResponse = jsonMapper.toJson(response);
        claimRepository.saveDefendantResponse(claim.getExternalId(), defendantEmail, defendantResponse);
    }

    @Override
    public List<Claim> getByDefendantId(String id, String authorisation) {
        return claimRepository.getByDefendantId(id);
    }

    @Override
    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        return claimRepository.getByLetterHolderId(id);
    }

    @Override
    public void requestMoreTimeForResponse(String authorisation, Claim claim, LocalDate newResponseDeadline) {
        claimRepository.requestMoreTime(claim.getExternalId(), newResponseDeadline);
    }

    @Override
    public void updateSettlement(
        Claim claim,
        Settlement settlement,
        String authorisation,
        String userAction
    ) {
        offersRepository.updateSettlement(claim.getExternalId(), jsonMapper.toJson(settlement));
    }

    @Override
    public void reachSettlementAgreement(Claim claim, Settlement settlement, String authorisation, String userAction) {
        offersRepository.reachSettlement(
            claim.getExternalId(),
            jsonMapper.toJson(settlement),
            LocalDateTimeFactory.nowInUTC()
        );
    }

    @Override
    public Claim saveClaim(String authorisation, Claim claim) {
        String claimDataString = jsonMapper.toJson(claim.getClaimData());
        if (claim.getClaimData().isClaimantRepresented()) {
            claimRepository.saveRepresented(claimDataString, claim.getSubmitterId(), claim.getIssuedOn(),
                claim.getResponseDeadline(), claim.getExternalId(), claim.getSubmitterEmail());
        } else {
            claimRepository.saveSubmittedByClaimant(claimDataString, claim.getSubmitterId(), claim.getLetterHolderId(),
                claim.getIssuedOn(), claim.getResponseDeadline(), claim.getExternalId(), claim.getSubmitterEmail());
        }

        return claimRepository
            .getClaimByExternalId(claim.getExternalId())
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + claim.getExternalId()));
    }
}
