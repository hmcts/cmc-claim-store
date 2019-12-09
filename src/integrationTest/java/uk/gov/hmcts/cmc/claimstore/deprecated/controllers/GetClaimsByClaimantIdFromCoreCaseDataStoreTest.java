package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseGetTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.PaginatedSearchMetadata;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.caseWithReferenceNumber;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.listOfCaseDetails;

@Ignore
public class GetClaimsByClaimantIdFromCoreCaseDataStoreTest extends BaseGetTest {
    private static final String SERVICE_TOKEN = "S2S token";
    private static final String USER_ID = "1";

    private static final UserDetails CITIZEN_USER_DETAILS = SampleUserDetails.builder()
        .withUserId(USER_ID)
        .withMail("submitter@example.com")
        .build();

    private static final UserDetails SOLICITOR_USER_DETAILS = SampleUserDetails.builder()
        .withUserId(USER_ID)
        .withRoles("solicitor")
        .withMail("submitter@example.com")
        .build();

    private static final User CITIZEN_USER = new User(AUTHORISATION_TOKEN, CITIZEN_USER_DETAILS);
    private static final User SOLICITOR_USER = new User(AUTHORISATION_TOKEN, SOLICITOR_USER_DETAILS);

    @Before
    public void before() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldFindClaimFromCCDForClaimantId() throws Exception {
        String submitterId = "20";
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(CITIZEN_USER);

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            any()
            )
        ).willReturn(listOfCaseDetails());

        MvcResult result = makeRequest("/claims/claimant/" + submitterId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .hasSize(1).first()
            .extracting(Claim::getSubmitterId).isEqualTo(submitterId);

        verify(coreCaseDataApi)
            .searchForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                any()
            );
    }

    @Test
    public void shouldPreserveOrderReturnedFromCCD() throws Exception {
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(CITIZEN_USER);

        CaseDetails caseDetails = caseWithReferenceNumber("000MC001");
        CaseDetails caseDetails1 = caseWithReferenceNumber("000MC002");
        CaseDetails caseDetails2 = caseWithReferenceNumber("000MC003");
        given(coreCaseDataApi.searchForCitizen(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
            )
        ).willReturn(
            asList(
                caseDetails,
                caseDetails1,
                caseDetails2
            )
        );

        MvcResult result = makeRequest("/claims/claimant/" + USER_ID)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .extracting(Claim::getReferenceNumber)
            .isEqualTo(asList("000MC001", "000MC002", "000MC003"));
    }

    private ImmutableMap<String, String> searchCriteria(int page) {
        return ImmutableMap.of(
            "case.submitterId", USER_ID,
            "page", String.valueOf(page),
            "sortDirection", "desc"
        );
    }

    @Test
    public void shouldUseCCDPaginationApiSolicitor() throws Exception {
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(SOLICITOR_USER);

        given(coreCaseDataApi.searchForCaseworker(
            any(),
            any(),
            any(),
            any(),
            any(),
            eq(searchCriteria(1))
            )
        ).willReturn(numberOfClaimDetailsResults(11));

        given(coreCaseDataApi.searchForCaseworker(
            any(),
            any(),
            any(),
            any(),
            any(),
            eq(searchCriteria(2))
            )
        ).willReturn(numberOfClaimDetailsResults(5));

        PaginatedSearchMetadata searchMetadata = new PaginatedSearchMetadata();
        searchMetadata.setTotalPagesCount(2);
        searchMetadata.setTotalResultsCount(16);

        given(coreCaseDataApi.getPaginationInfoForSearchForCaseworkers(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
            )
        ).willReturn(searchMetadata);

        MvcResult result = makeRequest("/claims/claimant/" + USER_ID)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .hasSize(16);
    }

    @Test
    public void shouldUseCCDPaginationApiCitizen() throws Exception {
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(CITIZEN_USER);

        given(coreCaseDataApi.searchForCitizen(
            any(),
            any(),
            any(),
            any(),
            any(),
            eq(searchCriteria(1))
            )
        ).willReturn(numberOfClaimDetailsResults(11));

        given(coreCaseDataApi.searchForCitizen(
            any(),
            any(),
            any(),
            any(),
            any(),
            eq(searchCriteria(2))
            )
        ).willReturn(numberOfClaimDetailsResults(5));

        PaginatedSearchMetadata searchMetadata = new PaginatedSearchMetadata();
        searchMetadata.setTotalPagesCount(2);
        searchMetadata.setTotalResultsCount(16);

        given(coreCaseDataApi.getPaginationInfoForSearchForCitizens(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
            )
        ).willReturn(searchMetadata);

        MvcResult result = makeRequest("/claims/claimant/" + USER_ID)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .hasSize(16);
    }

    private List<CaseDetails> numberOfClaimDetailsResults(final int number) {
        return Stream.generate(ResourceLoader::successfulCoreCaseDataStoreSubmitResponse)
            .limit(number)
            .collect(Collectors.toList());
    }
}
