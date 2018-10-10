package uk.gov.hmcts.cmc.claimstore.repositories;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.JobSchedulerService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.CaseReference;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBCaseRepository implements CaseRepository {

    private final ClaimRepository claimRepository;
    private final OffersRepository offersRepository;
    private final JsonMapper jsonMapper;
    private final UserService userService;
    private final JobSchedulerService jobSchedulerService;

    public DBCaseRepository(
        ClaimRepository claimRepository,
        OffersRepository offersRepository,
        JsonMapper jsonMapper,
        UserService userService,
        JobSchedulerService jobSchedulerService
    ) {
        this.claimRepository = claimRepository;
        this.offersRepository = offersRepository;
        this.jsonMapper = jsonMapper;
        this.userService = userService;
        this.jobSchedulerService = jobSchedulerService;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return claimRepository.getBySubmitterId(submitterId);
    }

    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return claimRepository.getClaimByExternalId(externalId);
    }

    @Override
    public Long getOnHoldIdByExternalId(String externalId, String authorisation) {
        getClaimByExternalId(externalId, authorisation)
            .ifPresent(claim -> {
                throw new ConflictException("Duplicate claim for external id " + claim.getExternalId());
            });

        return null;
    }

    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        if (authorisation != null) {
            String submitterId = userService.getUserDetails(authorisation).getId();
            return claimRepository.getByClaimReferenceAndSubmitter(claimReferenceNumber, submitterId);
        }

        return claimRepository.getByClaimReferenceNumber(claimReferenceNumber);
    }

    @Override
    public void linkDefendant(String authorisation) {
        User defendantUser = userService.getUser(authorisation);
        String defendantId = defendantUser.getUserDetails().getId();
        String defendantEmail = defendantUser.getUserDetails().getEmail();

        defendantUser.getUserDetails().getRoles().stream()
            .filter(this::isLetterHolderRole)
            .map(this::extractLetterHolderId)
            .forEach(letterHolderId -> {
                Integer noOfRows = claimRepository.linkDefendant(letterHolderId, defendantId, defendantEmail);
                if (noOfRows != 0) {
                    claimRepository.getByLetterHolderId(letterHolderId)
                        .ifPresent(jobSchedulerService::scheduleEmailNotificationsForDefendantResponse);
                }
            });
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
    public void saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment,
        boolean issue
    ) {
        final String externalId = claim.getExternalId();
        LocalDateTime ccjIssuedDate = issue ? nowInUTC() : null;

        claimRepository.saveCountyCourtJudgment(externalId,
            jsonMapper.toJson(countyCourtJudgment), nowInUTC(), ccjIssuedDate
        );
    }

    @Override
    public void saveDefendantResponse(Claim claim, String defendantEmail, Response response, String authorization) {
        String defendantResponse = jsonMapper.toJson(response);
        claimRepository.saveDefendantResponse(claim.getExternalId(), defendantEmail, defendantResponse);
    }

    @Override
    public void saveClaimantResponse(Claim claim, ClaimantResponse response, String authorization) {
        claimRepository.saveClaimantResponse(claim.getExternalId(), jsonMapper.toJson(response));
    }

    @Override
    public void saveMoneyReceivedOn(Claim claim, PaidInFull paidInFull, String authorization) {
        claimRepository.saveMoneyReceivedOn(claim.getExternalId(), paidInFull.getMoneyReceivedOn());
    }

    @Override
    public void updateDirectionsQuestionnaireDeadline(String externalId, LocalDate dqDeadline, String authorization) {
        claimRepository.updateDirectionsQuestionnaireDeadline(externalId, dqDeadline);
    }

    @Override
    public List<Claim> getByDefendantId(String id, String authorisation) {
        return claimRepository.getByDefendantId(id);
    }

    @Override
    public List<Claim> getByClaimantEmail(String email, String authorisation) {
        return claimRepository.getBySubmitterEmail(email);
    }

    @Override
    public List<Claim> getByDefendantEmail(String email, String authorisation) {
        return claimRepository.getByDefendantEmail(email);
    }

    @Override
    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        return claimRepository.getByLetterHolderId(id);
    }

    @Override
    public void requestMoreTimeForResponse(String authorisation, Claim claim, LocalDate newResponseDeadline) {
        claimRepository.requestMoreTime(claim.getExternalId(), newResponseDeadline);
        jobSchedulerService.rescheduleEmailNotificationsForDefendantResponse(claim, newResponseDeadline);
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
            nowInUTC()
        );
    }

    /**
     * For non-CCD datastore it always new CaseReference(externalId) as there is no pre payment step for non-ccd env.
     */
    @Override
    public CaseReference savePrePaymentClaim(String externalId, String authorisation) {
        return new CaseReference(externalId);
    }

    @Override
    public Claim saveClaim(String authorisation, Claim claim) {
        String claimDataString = jsonMapper.toJson(claim.getClaimData());
        String features = jsonMapper.toJson(claim.getFeatures());
        if (claim.getClaimData().isClaimantRepresented()) {
            claimRepository.saveRepresented(claimDataString, claim.getSubmitterId(), claim.getIssuedOn(),
                claim.getResponseDeadline(), claim.getExternalId(), claim.getSubmitterEmail(), features);
        } else {
            claimRepository.saveSubmittedByClaimant(claimDataString, claim.getSubmitterId(), claim.getLetterHolderId(),
                claim.getIssuedOn(), claim.getResponseDeadline(), claim.getExternalId(),
                claim.getSubmitterEmail(), features);
        }

        return claimRepository
            .getClaimByExternalId(claim.getExternalId())
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + claim.getExternalId()));
    }

    @Override
    public void linkSealedClaimDocument(String authorisation, Claim claim, URI documentUri) {
        claimRepository.linkSealedClaimDocument(claim.getId(), documentUri.toString());
    }
}
