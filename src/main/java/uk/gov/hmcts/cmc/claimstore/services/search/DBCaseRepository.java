package uk.gov.hmcts.cmc.claimstore.services.search;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.repositories.LegacyClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBCaseRepository implements CaseRepository {

    private final LegacyClaimRepository legacyClaimRepository;
    private final UserService userService;

    public DBCaseRepository(
        LegacyClaimRepository legacyClaimRepository,
        UserService userService
    ) {
        this.legacyClaimRepository = legacyClaimRepository;
        this.userService = userService;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return legacyClaimRepository.getBySubmitterId(submitterId);
    }

    @Override
    public Optional<Claim> getByExternalId(String externalId, String authorisation) {
        return legacyClaimRepository.getByExternalId(externalId);
    }

    @Override
    public Optional<Claim> getByReferenceNumber(String claimReferenceNumber, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();
        return legacyClaimRepository.getByReferenceAndSubmitter(claimReferenceNumber, submitterId);
    }

    @Override
    public void linkDefendant(String externalId, String defendantId, String authorisation) {
        Claim claim = legacyClaimRepository.getByExternalId(externalId)
            .orElseThrow(() -> new NotFoundException("Claim not found by externalId: " + externalId));
        legacyClaimRepository.linkDefendant(claim.getId(), defendantId);
    }

    @Override
    public Long saveSubmittedByClaimant(
        String claim,
        String submitterId,
        String letterHolderId,
        LocalDate issuedOn,
        LocalDate responseDeadline,
        String externalId,
        String submitterEmail
    ) {
        return legacyClaimRepository.saveSubmittedByClaimant(
            claim, submitterId, letterHolderId, issuedOn,
            responseDeadline, externalId, submitterEmail
        );
    }

    @Override
    public Long saveRepresented(
        String claim,
        String submitterId,
        LocalDate issuedOn,
        LocalDate responseDeadline,
        String externalId,
        String submitterEmail
    ) {
        return legacyClaimRepository.saveRepresented(
            claim, submitterId, issuedOn,
            responseDeadline, externalId, submitterEmail
        );
    }
}
