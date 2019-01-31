package uk.gov.hmcts.cmc.claimstore.repositories;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.utils.CCDCaseDataToClaim;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchCriteria;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("searchRepository")
@ConditionalOnProperty(prefix = "feature_toggles", name = "es_search_enabled", havingValue = "true")
public class ElasticSearchRepository implements SearchRepository {
    public static final String CASE_FORMAT = "data.%s";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private CoreCaseDataApi coreCaseDataApi;
    private AuthTokenGenerator authTokenGenerator;
    private CCDCaseDataToClaim ccdCaseDataToClaim;

    public ElasticSearchRepository(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        CCDCaseDataToClaim ccdCaseDataToClaim
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.ccdCaseDataToClaim = ccdCaseDataToClaim;
    }

    @Override
    public List<Claim> getAllCasesBy(User user, ImmutableMap<String, String> searchString) {
        return extractClaims(getCaseDetails(searchString, user.getAuthorisation()));
    }

    @Override
    public Optional<Claim> getCaseBy(String authorisation, Map<String, String> searchString) {

        List<Claim> claims = extractClaims(getCaseDetails(searchString, authorisation));

        if (claims.size() > 1) {
            throw new CoreCaseDataStoreException("More than one claim found by search string " + searchString);
        }

        return claims.stream().findAny();
    }

    @Override
    public List<CaseDetails> searchAll(User user, Map<String, String> searchString) {
        String authorisation = user.getAuthorisation();
        return getCaseDetails(searchString, authorisation);
    }

    private List<CaseDetails> getCaseDetails(Map<String, String> searchString, String authorisation) {

        String searchParams = null;
        for (Map.Entry<String, String> entry : searchString.entrySet()) {
            searchParams = SearchCriteria.searchByQuery(
                String.format(CASE_FORMAT, entry.getKey()),
                entry.getValue()
            );
        }

        String serviceAuthToken = this.authTokenGenerator.generate();
        SearchResult searchResult = coreCaseDataApi.searchCases(authorisation,
            serviceAuthToken,
            CASE_TYPE_ID,
            searchParams != null ? searchParams : SearchCriteria.matchAllQuery()
        );

        return searchResult.getCases();
    }

    private List<Claim> extractClaims(List<CaseDetails> result) {
        return result
            .stream()
            .filter(entry -> !entry.getState().equals(CCDCaseApi.CaseState.ONHOLD.getValue()))
            .map(entry -> ccdCaseDataToClaim.to(entry.getId(), entry.getData()))
            .collect(Collectors.toList());
    }

}
