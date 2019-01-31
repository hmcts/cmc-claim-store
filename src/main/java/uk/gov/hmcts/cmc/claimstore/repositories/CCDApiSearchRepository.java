package uk.gov.hmcts.cmc.claimstore.repositories;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.utils.CCDCaseDataToClaim;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.*;
import java.util.stream.Collectors;

@Service("searchRepository")
@ConditionalOnProperty(prefix = "feature_toggles", name = "es_search_enabled", havingValue = "false")
public class CCDApiSearchRepository implements SearchRepository {
    // CCD has a page size of 25 currently, it is configurable so assume it'll never be less than 10
    private static final int MINIMUM_SIZE_TO_CHECK_FOR_MORE_PAGES = 10;
    private static final int MAX_NUM_OF_PAGES_TO_CHECK = 10;
    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";
    public static final String CASE_FORMAT = "case.%s";

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private CCDCaseDataToClaim ccdCaseDataToClaim;
    private UserService userService;

    public CCDApiSearchRepository(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        CCDCaseDataToClaim ccdCaseDataToClaim,
        UserService userService
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.ccdCaseDataToClaim = ccdCaseDataToClaim;
        this.userService = userService;
    }

    @Override
    public List<Claim> getAllCasesBy(User user, ImmutableMap<String, String> searchString) {
         List<CaseDetails> validCases = searchAll(user, searchString)
            .stream()
            .filter(c -> !isCaseOnHold(c))
            .collect(Collectors.toList());

        return extractClaims(validCases);
    }

    @Override
    public Optional<Claim> getCaseBy(String authorisation, Map<String, String> searchString) {
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

    @Override
    public List<CaseDetails> searchAll(User user, Map<String, String> searchString) {
        return search(user, searchString, 1, new ArrayList<>(), null, null);
    }

    @SuppressWarnings("ParameterAssignment") // recursively modifying it internally only
    private List<CaseDetails> search(
        User user,
        Map<String, String> searchString,
        Integer page,
        List<CaseDetails> results,
        Integer numOfPages,
        CCDCaseApi.CaseState state
    ) {
        Map<String, String> searchParams = searchString.entrySet().stream()
            .collect(Collectors.toMap(e -> String.format(CASE_FORMAT, e.getKey()), Map.Entry::getValue));

        Map<String, String> searchCriteria = new HashMap<>(searchParams);
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
        if (user.getUserDetails().isSolicitor() || user.getUserDetails().isCaseworker()) {

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
        if (user.getUserDetails().isSolicitor() || user.getUserDetails().isCaseworker()) {
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
        return caseDetails.getState().equals(CCDCaseApi.CaseState.ONHOLD.getValue());
    }
}
