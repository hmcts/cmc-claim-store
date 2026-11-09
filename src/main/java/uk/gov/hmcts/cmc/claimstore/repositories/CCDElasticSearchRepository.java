package uk.gov.hmcts.cmc.claimstore.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.Queries;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.Query;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.SearchClause;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.DateUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_CITIZEN_PAYMENT;

@Repository("searchRepository")
public class CCDElasticSearchRepository implements CaseSearchApi {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String CLAIM_STATE = "state";

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

        logger.info("MILO: ElasticSearch query to fetch data at {} for the date {} ",
            LocalDateTime.now(), mediationAgreedDate);

        Query mediationQuery = new Query(
            Queries.boolQuery()
                .must(Queries.termQuery(
                    "data.respondents.value.responseFreeMediationOption", CCDYesNoOption.YES.name()))
                .must(Queries.termQuery(
                    "data.respondents.value.claimantResponse.freeMediationOption", CCDYesNoOption.YES.name()))
                .must(Queries.rangeQuery("data.respondents.value.claimantResponse.submittedOn")
                    .from(DateUtils.startOfDay(mediationAgreedDate), true)
                    .to(DateUtils.endOfDay(mediationAgreedDate), true)), 500, 0
        );

        return searchClaimsWith(user, mediationQuery);

    }

    public List<Claim> getClaimsWithDefaultCCJ(User user, LocalDate ccjRequestedDate) {

        Query mediationQuery = new Query(
            Queries.boolQuery()
                .must(Queries.matchQuery(
                    "data.respondents.value.countyCourtJudgmentRequest.type",
                    CountyCourtJudgmentType.DEFAULT.name()))
                .must(Queries.rangeQuery("data.respondents.value.countyCourtJudgmentRequest.requestedDate")
                    .from(DateUtils.startOfDay(ccjRequestedDate), true)
                    .to(DateUtils.endOfDay(ccjRequestedDate), true)), 1000, 0
        );

        return searchClaimsWith(user, mediationQuery);

    }

    public Integer getClaimCountForClaimant(String submitterId, User user) {
        Query getClaimsForClaimant = new Query(Queries.boolQuery()
            .must(Queries.matchQuery("data.submitterId", submitterId))
            .mustNot(Queries.matchQuery(CLAIM_STATE, AWAITING_CITIZEN_PAYMENT.getValue().toLowerCase())),
            10, 0);

        return getClaimsCountById(user, getClaimsForClaimant);
    }

    public Integer getClaimCountForDefendant(String submitterId, User user) {

        Query getClaimsForClaimant = new Query(Queries.boolQuery()
            .must(Queries.matchQuery("data.respondents.value.defendantId", submitterId)),
            10, 0);

        return getClaimsCountById(user, getClaimsForClaimant);
    }

    public List<Claim> getClaimsForClaimant(String submitterId, User user, int index) {

        Query getClaimsForClaimant = new Query(Queries.boolQuery()
            .must(Queries.matchQuery("data.submitterId", submitterId))
            .mustNot(Queries.matchQuery(CLAIM_STATE, AWAITING_CITIZEN_PAYMENT.getValue().toLowerCase())),
            10, index);
        return searchClaimsWith(user, getClaimsForClaimant);
    }

    public List<Claim> getClaimsForDefendant(String submitterId, User user, int index) {

        Query getClaimsForClaimant = new Query(Queries.boolQuery()
            .must(Queries.matchQuery("data.respondents.value.defendantId", submitterId)),
            10, index);

        return searchClaimsWith(user, getClaimsForClaimant);
    }

    @Override
    public List<Claim> getClaimsReadyForTransfer(User user) {
        Query readyForTransferQuery = new Query(Queries.boolQuery()
            .must(Queries.termQuery(CLAIM_STATE, ClaimState.READY_FOR_TRANSFER.getValue().toLowerCase()))
            .must(Queries.existsQuery("data.hearingCourtName"))
            .must(Queries.existsQuery("data.hearingCourtAddress")), 1000, 0);
        return searchClaimsWith(user, readyForTransferQuery);
    }

    @Override
    public List<Claim> getClaims(User user, SearchClause queryBuilder) {
        return searchClaimsWith(user, new Query(queryBuilder, 1000, 0));
    }

    private List<Claim> searchClaimsWith(User user, Query query) {
        String serviceAuthToken = this.authTokenGenerator.generate();

        SearchResult searchResult = coreCaseDataApi.searchCases(
            user.getAuthorisation(),
            serviceAuthToken,
            CASE_TYPE_ID,
            query.toString()
        );

        return  searchResult.getCases()
            .stream()
            .map(ccdCaseDetailsConverter::extractClaim)
            .collect(Collectors.toList());
    }

    private Integer getClaimsCountById(User user, Query query) {
        String serviceAuthToken = this.authTokenGenerator.generate();

        SearchResult searchResult = coreCaseDataApi.searchCases(
            user.getAuthorisation(),
            serviceAuthToken,
            CASE_TYPE_ID,
            query.toString()
        );

        return searchResult.getTotal();
    }
}
