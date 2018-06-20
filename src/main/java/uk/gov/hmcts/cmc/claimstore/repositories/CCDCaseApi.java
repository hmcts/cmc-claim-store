package uk.gov.hmcts.cmc.claimstore.repositories;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.exceptions.DefendantLinkingException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.exceptions.OnHoldClaimAccessAttemptException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.utils.CCDCaseDataToClaim;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.UserId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseApi {

    public static enum CaseState {
        ONHOLD("onhold"),
        OPEN("open");

        private final String state;

        CaseState(String state) {
            this.state = state;
        }

        public String getValue() {
            return state;
        }
    }

    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CaseAccessApi caseAccessApi;
    private final CoreCaseDataService coreCaseDataService;
    private final CCDCaseDataToClaim ccdCaseDataToClaim;

    private static final Logger LOGGER = LoggerFactory.getLogger(CCDCaseApi.class);
    // CCD has a page size of 25 currently, it is configurable so assume it'll never be less than 10
    private static final int MINIMUM_SIZE_TO_CHECK_FOR_MORE_PAGES = 10;
    private static final int MAX_NUM_OF_PAGES_TO_CHECK = 10;

    public CCDCaseApi(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        CaseAccessApi caseAccessApi,
        CoreCaseDataService coreCaseDataService,
        CCDCaseDataToClaim ccdCaseDataToClaim
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.caseAccessApi = caseAccessApi;
        this.coreCaseDataService = coreCaseDataService;
        this.ccdCaseDataToClaim = ccdCaseDataToClaim;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        User user = userService.getUser(authorisation);

        return getAllCasesBy(user, ImmutableMap.of("case.submitterId", submitterId));
    }

    public Optional<Claim> getByReferenceNumber(String referenceNumber, String authorisation) {
        return getCaseBy(authorisation, ImmutableMap.of("case.referenceNumber", referenceNumber));
    }

    public Optional<Claim> getByExternalId(String externalId, String authorisation) {
        return getCaseBy(authorisation, ImmutableMap.of("case.externalId", externalId));
    }

    public List<Claim> getByDefendantId(String id, String authorisation) {
        User user = userService.getUser(authorisation);

        return getAllCasesBy(user, ImmutableMap.of("case.defendantId", id));
    }

    public Long getOnHoldIdByExternalId(String externalId, String authorisation) {
        User user = userService.getUser(authorisation);
        List<CaseDetails> result = searchAll(user, ImmutableMap.of("case.externalId", externalId));

        if (result.size() == 0) {
            throw new NotFoundException("Case " + externalId + " not found.");
        }

        CaseDetails ccd = result.get(0);

        if (!isCaseOnHold(ccd)) {
            throw new OnHoldClaimAccessAttemptException("Case " + externalId + " is not on hold.");
        }

        return ccd.getId();
    }

    /**
     * LLD https://tools.hmcts.net/confluence/display/ROC/Defendant+linking+with+CCD
     */
    public void linkDefendant(String authorisation) {
        User defendantUser = userService.getUser(authorisation);
        List<String> letterHolderIds = defendantUser.getUserDetails().getRoles()
            .stream()
            .filter(this::isLetterHolderRole)
            .map(this::extractLetterHolderId)
            .collect(Collectors.toList());

        if (letterHolderIds.isEmpty()) {
            return;
        }

        User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();

        letterHolderIds
            .forEach(letterHolderId -> caseAccessApi.findCaseIdsGivenUserIdHasAccessTo(
                anonymousCaseWorker.getAuthorisation(),
                authTokenGenerator.generate(),
                anonymousCaseWorker.getUserDetails().getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                letterHolderId
            ).forEach(caseId -> linkToCase(defendantUser, anonymousCaseWorker, letterHolderId, caseId)));
    }

    private List<Claim> getAllCasesBy(User user, ImmutableMap<String, String> searchString) {
        List<CaseDetails> validCases = searchAll(user, searchString)
            .stream()
            .filter(c -> !isCaseOnHold(c))
            .collect(Collectors.toList());

        return extractClaims(validCases);
    }

    private Optional<Claim> getCaseBy(String authorisation, Map<String, String> searchString) {
        User user = userService.getUser(authorisation);

        List<CaseDetails> result = searchAll(user, searchString);

        if (result.size() == 1 && isCaseOnHold(result.get(0))) {
            return Optional.empty();
        }

        List<Claim> claims = extractClaims(result);

        if (claims.size() > 1) {
            throw new CoreCaseDataStoreException("More than one claim found by search string " + searchString);
        }

        return claims.stream().findAny();
    }

    private void linkToCase(User defendantUser, User anonymousCaseWorker, String letterHolderId, String caseId) {
        String defendantId = defendantUser.getUserDetails().getId();
        LOGGER.info("Granting access to case: {} for user: {} with letter-holder id: {}",
            caseId, defendantId, letterHolderId);
        caseAccessApi.grantAccessToCase(anonymousCaseWorker.getAuthorisation(),
            authTokenGenerator.generate(),
            anonymousCaseWorker.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            caseId,
            new UserId(defendantId)
        );

        LOGGER.info("Revoking access to case: {} for user: {}", caseId, letterHolderId);
        caseAccessApi.revokeAccessToCase(anonymousCaseWorker.getAuthorisation(),
            authTokenGenerator.generate(),
            anonymousCaseWorker.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            caseId,
            letterHolderId
        );

        coreCaseDataService.update(
            defendantUser.getAuthorisation(),
            CCDCase.builder().id(Long.valueOf(caseId)).defendantId(defendantId).build(),
            CaseEvent.LINK_DEFENDANT
        );
    }

    private boolean isLetterHolderRole(String role) {
        Objects.requireNonNull(role);
        return role.startsWith("letter")
            && !role.equals("letter-holder")
            && !role.endsWith("loa1");
    }

    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();

        List<String> letterHolderCases = caseAccessApi.findCaseIdsGivenUserIdHasAccessTo(
            anonymousCaseWorker.getAuthorisation(),
            authTokenGenerator.generate(),
            anonymousCaseWorker.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            id
        );
        if (letterHolderCases.size() > 1) {
            throw new DefendantLinkingException("More than 1 case a letter holder ID has access to found");
        }
        if (letterHolderCases.isEmpty()) {
            return Optional.empty();
        }

        User letterHolder = userService.getUser(authorisation);
        return Optional.of(readCase(letterHolder, letterHolderCases.get(0)));
    }

    private String extractLetterHolderId(String role) {
        return StringUtils.remove(role, "letter-");
    }

    private Claim readCase(User user, String caseId) {
        CaseDetails caseDetails = coreCaseDataApi.readForCitizen(
            user.getAuthorisation(),
            authTokenGenerator.generate(),
            user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            caseId
        );
        return ccdCaseDataToClaim.to(caseDetails.getId(), caseDetails.getData());
    }

    private List<CaseDetails> searchAll(User user, Map<String, String> searchString) {
        return search(user, searchString, 1, new ArrayList<>(), null, null);
    }

    @SuppressWarnings("ParameterAssignment") // recursively modifying it internally only
    private List<CaseDetails> search(
        User user,
        Map<String, String> searchString,
        Integer page,
        List<CaseDetails> results,
        Integer numOfPages,
        CaseState state
    ) {
        Map<String, String> searchCriteria = new HashMap<>(searchString);
        searchCriteria.put("page", page.toString());
        searchCriteria.put("sortDirection", "desc");
        if (state != null) {
            searchCriteria.put("state", state.getValue());
        }

        String serviceAuthToken = this.authTokenGenerator.generate();

        results.addAll(performSearch(user, searchCriteria, serviceAuthToken));

        if (results.size() > MINIMUM_SIZE_TO_CHECK_FOR_MORE_PAGES) {
            if (numOfPages == null) {
                numOfPages = getTotalPagesCount(user, searchCriteria, serviceAuthToken);
            }

            if (numOfPages > page && page < MAX_NUM_OF_PAGES_TO_CHECK) {
                ++page;
                return search(user, searchCriteria, page, results, numOfPages, state);
            }
        }

        return results;
    }

    private List<CaseDetails> performSearch(User user, Map<String, String> searchCriteria, String serviceAuthToken) {
        List<CaseDetails> result;
        if (user.getUserDetails().isSolicitor()) {

            result = coreCaseDataApi.searchForCaseworker(
                user.getAuthorisation(),
                serviceAuthToken,
                user.getUserDetails().getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                searchCriteria
            );
        } else {
            result = coreCaseDataApi.searchForCitizen(
                user.getAuthorisation(),
                serviceAuthToken,
                user.getUserDetails().getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                searchCriteria
            );
        }
        return result;
    }

    private int getTotalPagesCount(User user, Map<String, String> searchCriteria, String serviceAuthToken) {
        int result;
        if (user.getUserDetails().isSolicitor()) {
            result = coreCaseDataApi
                .getPaginationInfoForSearchForCaseworkers(
                    user.getAuthorisation(),
                    serviceAuthToken,
                    user.getUserDetails().getId(),
                    JURISDICTION_ID,
                    CASE_TYPE_ID,
                    searchCriteria
                ).getTotalPagesCount();
        } else {
            result = coreCaseDataApi
                .getPaginationInfoForSearchForCitizens(
                    user.getAuthorisation(),
                    serviceAuthToken,
                    user.getUserDetails().getId(),
                    JURISDICTION_ID,
                    CASE_TYPE_ID,
                    searchCriteria
                ).getTotalPagesCount();
        }

        return result;
    }

    private List<Claim> extractClaims(List<CaseDetails> result) {
        return result
            .stream()
            .map(entry -> ccdCaseDataToClaim.to(entry.getId(), entry.getData()))
            .collect(Collectors.toList());
    }

    private boolean isCaseOnHold(CaseDetails caseDetails) {
        return caseDetails.getState().equals(CaseState.ONHOLD.getValue());
    }
}
