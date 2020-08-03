package uk.gov.hmcts.cmc.claimstore.repositories;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.Query;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.DateUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_TRANSFER;

@Repository("searchRepository")
public class CCDElasticSearchRepository implements CaseSearchApi {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CaseDetailsConverter ccdCaseDetailsConverter;

    @Autowired
    public CCDElasticSearchRepository(CoreCaseDataApi coreCaseDataApi,
                                      AuthTokenGenerator authTokenGenerator,
                                      UserService userService,
                                      CaseDetailsConverter ccdCaseDataToClaim) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.ccdCaseDetailsConverter = ccdCaseDataToClaim;
    }

    public List<Claim> getMediationClaims(String authorisation, LocalDate mediationAgreedDate) {
        User user = userService.getUser(authorisation);

        Query mediationQuery = new Query(
            QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(
                    "data.respondents.value.responseFreeMediationOption", CCDYesNoOption.YES.name()))
                .must(QueryBuilders.termQuery(
                    "data.respondents.value.claimantResponse.freeMediationOption", CCDYesNoOption.YES.name()))
                .must(QueryBuilders.rangeQuery("data.respondents.value.claimantResponse.submittedOn")
                    .from(DateUtils.startOfDay(mediationAgreedDate), true)
                    .to(DateUtils.endOfDay(mediationAgreedDate), true)), 1000
        );

        return searchClaimsWith(user, mediationQuery);

    }

    public List<Claim> getClaimsWithDefaultCCJ(User user, LocalDate ccjRequestedDate) {

        Query mediationQuery = new Query(
            QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery(
                    "data.respondents.value.countyCourtJudgmentRequest.type",
                    CountyCourtJudgmentType.DEFAULT.name()))
                .must(QueryBuilders.rangeQuery("data.respondents.value.countyCourtJudgmentRequest.requestedDate")
                    .from(DateUtils.startOfDay(ccjRequestedDate), true)
                    .to(DateUtils.endOfDay(ccjRequestedDate), true)), 1000
        );

        return searchClaimsWith(user, mediationQuery);

    }

    @Override
    public List<CCDCase> getClaimsReadyForTransfer(User user, String... queryData) {
        Query readyForTransferQuery = new Query(QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("state", READY_FOR_TRANSFER.getValue().toLowerCase()))
            .must(QueryBuilders.existsQuery(queryData[0]))
            .must(QueryBuilders.existsQuery(queryData[1])), 2000);
        return searchClaims(user, readyForTransferQuery);
    }

    @Override
    public Integer totalClaimsReadyForTransfer(User user) {
        Query readyForTransferQuery = new Query(QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("state", READY_FOR_TRANSFER.getValue().toLowerCase())), 2000);
        return searchClaimsForUser(user, readyForTransferQuery).getCases().size();
    }

    @Override
    public List<Claim> getClaims(User user, QueryBuilder queryBuilder) {
        return searchClaimsWith(user, new Query(queryBuilder, 1000));
    }

    private List<CCDCase> searchClaims(User user, Query query) {
        SearchResult searchResult = searchClaimsForUser(user, query);
        return searchResult.getCases()
            .stream()
            .map(ccdCaseDetailsConverter::extractCCDCase)
            .collect(Collectors.toList());
    }

    private List<Claim> searchClaimsWith(User user, Query query) {
        SearchResult searchResult = searchClaimsForUser(user, query);
        return searchResult.getCases()
            .stream()
            .map(ccdCaseDetailsConverter::extractClaim)
            .collect(Collectors.toList());
    }

    private SearchResult searchClaimsForUser(User user, Query query) {
        return coreCaseDataApi.searchCases(
            user.getAuthorisation(),
            authTokenGenerator.generate(),
            CASE_TYPE_ID,
            query.toString()
        );
    }
}
