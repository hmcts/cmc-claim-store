package uk.gov.hmcts.cmc.claimstore.repositories;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.Query;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.SampleQueryConstants;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;

@RunWith(MockitoJUnitRunner.class)
public class CCDElasticSearchRepositoryTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String SERVICE_AUTH = "Authorization";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserService userService;
    @Mock
    private CaseDetailsConverter ccdCaseDetailsConverter;

    @Captor
    private ArgumentCaptor<String> queryStringCaptor;

    private CCDElasticSearchRepository ccdElasticSearchRepository;

    @Before
    public void setUp() {
        User mockUser = mock(User.class);
        ccdElasticSearchRepository = new CCDElasticSearchRepository(coreCaseDataApi, authTokenGenerator,
            userService, ccdCaseDetailsConverter);
        when(mockUser.getAuthorisation()).thenReturn(AUTHORISATION);
        when(userService.getUser(anyString())).thenReturn(mockUser);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH);

        when(coreCaseDataApi.searchCases(anyString(), anyString(), eq(CASE_TYPE_ID), anyString()))
            .thenReturn(SearchResult.builder().cases(ResourceLoader.listOfCaseDetails()).total(1).build());
    }

    @Test
    public void mediationSearchQueriesElastic() {
        ccdElasticSearchRepository.getMediationClaims(AUTHORISATION,
            LocalDate.of(2019, 7, 7));
        verify(userService, times(1)).getUser(anyString());
        verify(coreCaseDataApi).searchCases(
            eq(AUTHORISATION),
            eq(SERVICE_AUTH),
            eq(CASE_TYPE_ID),
            queryStringCaptor.capture());
        assertEquals(SampleQueryConstants.mediationQuery, queryStringCaptor.getValue());
    }

    @Test
    public void getClaimsShouldCallCoreCaseDataApi() {
        User user = new User(AUTHORISATION, null);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        ccdElasticSearchRepository.getClaims(user, queryBuilder);
        verify(coreCaseDataApi).searchCases(
            eq(AUTHORISATION),
            eq(SERVICE_AUTH),
            eq(CASE_TYPE_ID),
            eq(new Query(queryBuilder, 1000).toString())
        );
    }

    @Test
    public void ccjCasesWithDefaultCcjTenDaysPriorQueriesElastic() {
        User user = new User(AUTHORISATION, null);
        ccdElasticSearchRepository.getClaimsWithDefaultCCJ(user,
            LocalDate.of(2020, 01, 10));
        verify(coreCaseDataApi).searchCases(
            eq(AUTHORISATION),
            eq(SERVICE_AUTH),
            eq(CASE_TYPE_ID),
            queryStringCaptor.capture());
        assertEquals(SampleQueryConstants.defaultCCJCases10DaysBefore, queryStringCaptor.getValue());
    }

}
