package uk.gov.hmcts.cmc.claimstore.repositories;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.JobSchedulerService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.CREATE;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBCaseRepository implements CaseRepository {

    private final ClaimRepository claimRepository;
    private final OffersRepository offersRepository;
    private final JsonMapper jsonMapper;
    private final UserService userService;
    private final JobSchedulerService jobSchedulerService;
    private final boolean saveClaimStateEnabled;

    public DBCaseRepository(
        ClaimRepository claimRepository,
        OffersRepository offersRepository,
        JsonMapper jsonMapper,
        UserService userService,
        JobSchedulerService jobSchedulerService,
        @Value("${feature_toggles.save_claim_state_enabled:false}") boolean saveClaimStateEnabled
    ) {
        this.claimRepository = claimRepository;
        this.offersRepository = offersRepository;
        this.jsonMapper = jsonMapper;
        this.userService = userService;
        this.jobSchedulerService = jobSchedulerService;
        this.saveClaimStateEnabled = saveClaimStateEnabled;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return claimRepository.getBySubmitterId(submitterId);
    }

    @LogExecutionTime
    public Optional<Claim> getClaimByExternalId(String externalId, User user) {
        return claimRepository.getClaimByExternalId(externalId);
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
        CountyCourtJudgment countyCourtJudgment
    ) {
        final String externalId = claim.getExternalId();

        claimRepository.saveCountyCourtJudgment(externalId, jsonMapper.toJson(countyCourtJudgment), nowInUTC());
    }

    @Override
    public void saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        LocalDate claimantResponseDeadline,
        String authorization
    ) {
        String defendantResponse = jsonMapper.toJson(response);
        claimRepository.saveDefendantResponse(
            claim.getExternalId(),
            defendantEmail,
            claimantResponseDeadline,
            defendantResponse
        );
    }

    @Override
    public Claim saveClaimantResponse(Claim claim, ClaimantResponse response, String authorization) {
        claimRepository.saveClaimantResponse(claim.getExternalId(), jsonMapper.toJson(response));
        return claimRepository
            .getClaimByExternalId(claim.getExternalId())
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + claim.getExternalId()));
    }

    @Override
    public void paidInFull(Claim claim, PaidInFull paidInFull, String authorization) {
        claimRepository.updateMoneyReceivedOn(claim.getExternalId(), paidInFull.getMoneyReceivedOn());
    }

    @Override
    public void updateDirectionsQuestionnaireDeadline(Claim claim, LocalDate dqDeadline, String authorization) {
        claimRepository.updateDirectionsQuestionnaireDeadline(claim.getExternalId(), dqDeadline);
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
    public List<Claim> getByPaymentReference(String payReference, String authorisation) {
        return claimRepository.getByPaymentReference(payReference);
    }

    @Override
    public List<Claim> getClaimsByState(ClaimState claimState, User user) {
        return claimRepository.getClaimsByState(claimState);
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
        CaseEvent userAction
    ) {
        offersRepository.updateSettlement(claim.getExternalId(), jsonMapper.toJson(settlement));
    }

    @Override
    public void reachSettlementAgreement(Claim claim, Settlement settlement, String authorisation,
                                         CaseEvent caseEvent) {
        offersRepository.reachSettlement(
            claim.getExternalId(),
            jsonMapper.toJson(settlement),
            nowInUTC()
        );
    }

    @Override
    @LogExecutionTime
    public Claim saveClaim(User user, Claim claim) {
        String claimDataString = jsonMapper.toJson(claim.getClaimData());
        String features = jsonMapper.toJson(claim.getFeatures());
        String claimSubmissionOperationIndicator = jsonMapper.toJson(claim.getClaimSubmissionOperationIndicators());
        if (claim.getClaimData().isClaimantRepresented()) {
            claimRepository.saveRepresented(claimDataString, claim.getSubmitterId(), claim.getIssuedOn(),
                claim.getResponseDeadline(), claim.getExternalId(), claim.getSubmitterEmail(), features,
                claimSubmissionOperationIndicator);
        } else {
            ClaimState state = this.saveClaimStateEnabled ? CREATE : null;
            claimRepository.saveSubmittedByClaimant(claimDataString,
                claim.getSubmitterId(), claim.getLetterHolderId(),
                claim.getIssuedOn(), claim.getResponseDeadline(), claim.getExternalId(),
                claim.getSubmitterEmail(), features, state, claimSubmissionOperationIndicator);
        }

        return claimRepository
            .getClaimByExternalId(claim.getExternalId())
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + claim.getExternalId()));
    }

    @Override
    public Claim saveRepresentedClaim(User user, Claim claim) {
        throw new NotImplementedException("Not required to implement for claim store repository");
    }

    @Override
    public void saveReDetermination(
        String authorisation,
        Claim claim,
        ReDetermination reDetermination
    ) {
        claimRepository.saveReDetermination(claim.getExternalId(), jsonMapper.toJson(reDetermination));
    }

    @Override
    public Claim saveCaseEvent(String authorisation, Claim claim, CaseEvent caseEvent) {
        // No implementation required for claim-store repository
        return null;
    }

    @Override
    public Claim initiatePayment(User user, Claim claim) {
        // No implementation required for claim-store repository
        return null;
    }

    @Override
    public Claim saveCaseEventIOC(User user, Claim claim, CaseEvent caseEvent) {
        // No implementation required for claim-store repository
        return null;
    }

    @Override
    public Claim saveClaimDocuments(
        String authorisation,
        Long claimId,
        ClaimDocumentCollection claimDocumentCollection,
        ClaimDocumentType claimDocumentType
    ) {
        claimRepository.saveClaimDocuments(claimId, jsonMapper.toJson(claimDocumentCollection));
        Claim claim = getClaimById(claimId);
        String claimSubmissionOperationIndicators = jsonMapper.toJson(updateClaimSubmissionIndicatorByDocumentType(
            claim.getClaimSubmissionOperationIndicators(),
            claimDocumentType
        ));

        claimRepository.updateClaimSubmissionOperationStatus(claimId, claimSubmissionOperationIndicators);
        return getClaimById(claimId);
    }

    private ClaimSubmissionOperationIndicators updateClaimSubmissionIndicatorByDocumentType(
        ClaimSubmissionOperationIndicators indicators,
        ClaimDocumentType documentType
    ) {
        ClaimSubmissionOperationIndicators.ClaimSubmissionOperationIndicatorsBuilder updatedIndicator
            = indicators.toBuilder();

        if (documentType == SEALED_CLAIM) {
            updatedIndicator.sealedClaimUpload(YES);
        } else if (documentType == CLAIM_ISSUE_RECEIPT) {
            updatedIndicator.claimIssueReceiptUpload(YES);
        }
        return updatedIndicator.build();
    }

    @Override
    public Claim linkLetterHolder(Long claimId, String letterHolderId) {
        claimRepository.linkLetterHolder(claimId, letterHolderId);
        return getClaimById(claimId);
    }

    @Override
    public Claim saveReviewOrder(Long caseId, ReviewOrder reviewOrder, String authorisation) {
        throw new NotImplementedException("Save review order is not implemented for claim store database");
    }

    private Claim getClaimById(Long claimId) {
        return claimRepository.getById(claimId).orElseThrow(() ->
            new NotFoundException(String.format("Claim not found by primary key %s.", claimId)));
    }

    @Override
    public Claim updateClaimSubmissionOperationStatus(
        String authorisation,
        Long claimId,
        ClaimSubmissionOperationIndicators indicators,
        CaseEvent caseEvent) {
        claimRepository.updateClaimSubmissionOperationStatus(claimId, jsonMapper.toJson(indicators));
        return getClaimById(claimId);
    }

    @Override
    public void updateClaimState(String authorisation, Long claimId, ClaimState state) {
        claimRepository.updateClaimState(claimId, state.name());
    }
}
