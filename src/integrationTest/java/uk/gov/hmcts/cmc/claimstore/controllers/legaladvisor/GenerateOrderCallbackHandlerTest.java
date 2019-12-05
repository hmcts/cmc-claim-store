package uk.gov.hmcts.cmc.claimstore.controllers.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Address;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.MID;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponseWithDQ;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "doc_assembly.url=http://doc-assembly-api"
    }
)
public class GenerateOrderCallbackHandlerTest extends BaseMockSpringTest {

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withForename("legal")
        .withSurname("Advisor")
        .withRoles("caseworker-cmc-legaladvisor")
        .build();

    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final String DOCUMENT_URL = "http://bla.test";

    @Before
    public void setUp() {
        String serviceToken = "serviceToken";
        DocAssemblyResponse docAssemblyResponse = Mockito.mock(DocAssemblyResponse.class);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOCUMENT_URL);
        given(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
            .willReturn(ImmutableList.of(Court.builder()
                .name("Clerkenwell Court")
                .slug("clerkenwell-court")
                .address(Address.builder()
                    .addressLines(ImmutableList.of("line1", "line2"))
                    .postcode("SW1P4BB")
                    .town("Clerkenwell").build())
                .build()
            ));
        given(authTokenGenerator.generate()).willReturn(serviceToken);
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(USER_DETAILS);
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(new User(AUTHORISATION_TOKEN, USER_DETAILS));
        given(docAssemblyApi.generateOrder(
            anyString(),
            anyString(),
            any())).willReturn(docAssemblyResponse);
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEvent() throws Exception {
        MvcResult mvcResult = makeRequestGenerateOrder(ABOUT_TO_START.getValue())
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseData = jsonMappingHelper.deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        assertThat(responseData).hasSize(9);
        assertThat(LocalDate.parse(responseData.get("docUploadDeadline").toString()))
            .isAfterOrEqualTo(LocalDate.now().plusDays(42));
        assertThat(LocalDate.parse(responseData.get("eyewitnessUploadDeadline").toString()))
            .isAfterOrEqualTo(LocalDate.now().plusDays(42));
        assertThat(responseData).flatExtracting("directionList")
            .containsExactlyInAnyOrder("DOCUMENTS", "EYEWITNESS");
        assertThat(responseData.get("docUploadForParty")).isEqualTo("BOTH");
        assertThat(responseData.get("eyewitnessUploadForParty")).isEqualTo("BOTH");
        assertThat(responseData.get("preferredDQCourt")).isEqualTo("Preferred court");
        assertThat(responseData.get("paperDetermination")).isEqualTo("NO");
        assertThat(responseData.get("newRequestedCourt")).isNull();
        assertThat(responseData.get("preferredCourtObjectingParty")).isNull();
        assertThat(responseData.get("preferredCourtObjectingReason")).isNull();
        assertThat(responseData.get("otherDirectionHeaders")).isNull();
    }

    @Test
    public void shouldGenerateDocumentOnMidEvent() throws Exception {
        MvcResult mvcResult = makeRequest(MID.getValue())
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseData = jsonMappingHelper.deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();
        assertThat(responseData).hasSize(1);
        Map<String, String> document = (Map<String, String>) responseData.get("draftOrderDoc");
        assertThat(document.get("document_url")).isEqualTo(DOCUMENT_URL);
    }

    @Test
    public void shouldReturnErrorForUnknownCallback() throws Exception {
        MvcResult mvcResult = makeRequest("not-a-real-callback")
            .andExpect(status().isBadRequest())
            .andReturn();
        assertThat(mvcResult.getResolvedException())
            .isInstanceOfAny(CallbackException.class);
    }

    @Test
    public void shouldReturnErrorForUnsupportedRole() throws Exception {
        UserDetails userDetails = SampleUserDetails.builder()
            .withRoles("caseworker-cmc")
            .build();

        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);

        MvcResult mvcResult = makeRequest(ABOUT_TO_START.getValue())
            .andExpect(status().isForbidden())
            .andReturn();

        assertThat(mvcResult.getResolvedException())
            .isInstanceOfAny(ForbiddenActionException.class);
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CaseDetails caseDetailsTemp = successfulCoreCaseDataStoreSubmitResponseWithDQ();
        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .id(caseDetailsTemp.getId())
            .data(caseDetailsTemp.getData())
            .build();
        Map<String, Object> data = new HashMap<>(caseDetailsTemp.getData());
        data.put("paperDetermination", "No");
        data.put("docUploadDeadline", "2019-06-03");
        data.put("docUploadForParty", "BOTH");
        data.put("eyewitnessUploadDeadline", "2019-06-03");
        data.put("eyewitnessUploadForParty", "CLAIMANT");
        data.put("directionList", ImmutableList.of("EYEWITNESS", "DOCUMENTS"));
        data.put("extraDocUploadList", ImmutableList.of(ImmutableMap.of(
            "id", "",
            "value", "text")));
        data.put("otherDirections", ImmutableList.of(
            ImmutableMap.of(
                "id", "",
                "value", ImmutableMap.of(
                    "extraOrderDirection", "EYEWITNESS",
                    "sendBy", "2019-06-03",
                    "forParty", "CLAIMANT")),
            ImmutableMap.of(
                "id", "",
                "value", ImmutableMap.of(
                    "extraOrderDirection", "OTHER",
                    "directionComment", "second",
                    "sendBy", "2019-06-04",
                    "forParty", "BOTH"))));
        data.put("preferredDQCourt", "Preferred court");
        data.put("newRequestedCourt", "Another court");
        data.put("preferredCourtObjectingReason", "Because");
        data.put("hearingCourt", "CLERKENWELL");
        data.put("estimatedHearingDuration", "HALF_HOUR");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseDetailsTemp.getId())
            .data(data)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.GENERATE_ORDER.getValue())
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMappingHelper.toJson(callbackRequest))
            );
    }

    private ResultActions makeRequestGenerateOrder(String callbackType) throws Exception {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponseWithDQ();
        caseDetails.getData().put("preferredDQCourt", "Preferred court");

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.GENERATE_ORDER.getValue())
            .caseDetails(caseDetails)
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMappingHelper.toJson(callbackRequest))
            );
    }
}
