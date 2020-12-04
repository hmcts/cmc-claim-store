package uk.gov.hmcts.cmc.claimstore.tests.smoke;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDElasticSearchRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.SampleQueryConstants;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveCaseTest extends BaseTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String SERVICE_AUTH = "Authorization";
    private static final JsonMapper jsonMapper = JsonMapperFactory.create();

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserService userService;
    @Mock
    private CaseDetailsConverter ccdCaseDetailsConverter;
    private CCDElasticSearchRepository ccdElasticSearchRepository;

    @Before
    public void setUp() {
        ccdElasticSearchRepository = new CCDElasticSearchRepository(coreCaseDataApi, authTokenGenerator,
            userService, ccdCaseDetailsConverter);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH);

        when(coreCaseDataApi.searchCases(anyString(), anyString(), eq(CASE_TYPE_ID), anyString()))
            .thenReturn(SearchResult.builder().cases(listOfCaseDetails()).total(1).build());
    }

    public static List<CaseDetails> listOfCaseDetails() {
        String response = new ResourceReader().read("/data/search-response.success.json");
        return ImmutableList.of(jsonMapper.fromJson(response, CaseDetails.class));
    }

    @Test
    public void getClaimsForClaimantQueriesElastic() {
        User user = new User(AUTHORISATION, null);
        ccdElasticSearchRepository.getClaimsForClaimant(SampleClaim.USER_ID, user, 0);
        verify(coreCaseDataApi).searchCases(AUTHORISATION, SERVICE_AUTH, CASE_TYPE_ID,
            SampleQueryConstants.getClaimCountForClaimant);
    }

    @Test
    public void getClaimsForDefendantQueriesElastic() {
        User user = new User(AUTHORISATION, null);
        ccdElasticSearchRepository.getClaimsForDefendant(SampleClaim.USER_ID, user, 0);
        verify(coreCaseDataApi).searchCases(AUTHORISATION, SERVICE_AUTH, CASE_TYPE_ID,
            SampleQueryConstants.getClaimCountForDefendant);
    }

    /* commenting this out as currently there is no ES service running to validate this. Once ES service is
    built on AAT we can remove above mocks and use this
    @Test
    public void shouldBeAbleToRetrieveCasesBySubmitterId() {
        User citizen = bootstrap.getSmokeTestCitizen();
        testCasesRetrievalFor("/claims/claimant/" + citizen.getUserDetails().getId(),
            citizen.getAuthorisation());
    }

    @Test
    public void shouldBeAbleToRetrieveCasesByDefendantId() {
        User citizen = bootstrap.getSmokeTestCitizen();
        testCasesRetrievalFor("/claims/defendant/" + citizen.getUserDetails().getId(),
            citizen.getAuthorisation());
    }

    private void testCasesRetrievalFor(String uriPath, String authorisation) {
        String response = RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, authorisation)
            .when()
            .get(uriPath)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();
        System.out.println("Response is " + response);
        assertThat(response).matches(jsonListPattern);
    }*/
}
