package uk.gov.hmcts.cmc.claimstore.controllers.caseworker;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.MID;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "doc_assembly.url=http://doc-assembly-api",
        "feature_toggles.ctsc_enabled=true"
    }
)
public class ContactDetailsChangeCallbackHandlerTest extends BaseMockSpringTest {
    @MockBean
    protected EmailService emailService;

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withForename("cmc")
        .withSurname("caseworker")
        .withRoles("caseworker-cmc")
        .build();

    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final String DOCUMENT_BINARY_URL = "http://bla.test/binary";
    private static final String DOCUMENT_FILE_NAME = "contact-letter.pdf";
    @Mock
    private SendLetterResponse sendLetterResponse;

    @Before
    public void setUp() {
        String serviceToken = "serviceToken";
        DocAssemblyResponse docAssemblyResponse = Mockito.mock(DocAssemblyResponse.class);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOCUMENT_URL);

        given(authTokenGenerator.generate()).willReturn(serviceToken);
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(USER_DETAILS);
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(new User(AUTHORISATION_TOKEN, USER_DETAILS));
        given(docAssemblyApi.generateOrder(
            anyString(),
            anyString(),
            any())).willReturn(docAssemblyResponse);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldProcessMidEvent() throws Exception {
        MvcResult mvcResult = makeRequestContactDetailsChangeRequest(MID.getValue())
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseData = jsonMappingHelper.deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();
        assertThat(responseData).hasSize(2);
        Map<String, String> document = (Map<String, String>) responseData.get("draftLetterDoc");
        assertThat(document.get("document_url")).isEqualTo(DOCUMENT_URL);
        Map<String, String> contactChangeContent = (Map<String, String>) responseData.get("contactChangeContent");
        assertThat(contactChangeContent.get("primaryEmail")).isEqualTo("some@mail.com");
        assertThat(contactChangeContent.get("isEmailModified")).isEqualTo("YES");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldProcessAboutToSubmitEvent() throws Exception {
        given(documentManagementService.downloadDocument(anyString(), any(ClaimDocument.class)))
            .willReturn(new byte[] {1, 2, 3, 4});
        given(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class))).willReturn(sendLetterResponse);

        MvcResult mvcResult = makeRequest(ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseData = jsonMappingHelper.deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();
        List<Map<String, Object>> documents = (List<Map<String, Object>>) responseData.get("caseDocuments");

        assertThat(documents).hasSize(2);
        Map<String, Object> value = documents.get(1);
        Map<String, Object> claimDocument = (Map<String, Object>) value.get("value");
        assertThat(claimDocument.get("documentType")).isEqualTo(CCDClaimDocumentType.GENERAL_LETTER.name());
        Map<String, Object> document = (Map<String, Object>) claimDocument.get("documentLink");
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
            .withRoles("wrong-role")
            .build();

        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);

        MvcResult mvcResult = makeRequest(ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isForbidden())
            .andReturn();

        assertThat(mvcResult.getResolvedException())
            .isInstanceOfAny(ForbiddenActionException.class);

        mvcResult = makeRequest(MID.getValue())
            .andExpect(status().isForbidden())
            .andReturn();

        assertThat(mvcResult.getResolvedException())
            .isInstanceOfAny(ForbiddenActionException.class);
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CaseDetails caseDetailsTemp = successfulCoreCaseDataStoreSubmitResponse();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetailsTemp);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseDetailsTemp.getId())
            .state(caseDetailsTemp.getState())
            .data(caseDetailsConverter.convertToMap(ccdCase.toBuilder()
                .caseDocuments(ImmutableList.of(CCDCollectionElement.<CCDClaimDocument>builder()
                    .value(CCDClaimDocument.builder()
                        .documentType(CCDClaimDocumentType.SEALED_CLAIM)
                        .build())
                    .build()))
                .contactChangeParty(CCDContactPartyType.CLAIMANT)
                .draftLetterDoc(CCDDocument.builder()
                    .documentBinaryUrl(DOCUMENT_BINARY_URL)
                    .documentUrl(DOCUMENT_URL)
                    .documentFileName(DOCUMENT_FILE_NAME)
                    .build())
                .applicants(getApplicantWithContactDetailsModified(ccdCase))
                .respondents(getUnlinkedRespondent(ccdCase))
                .build()))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.CHANGE_CONTACT_DETAILS.getValue())
            .caseDetails(caseDetails)
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMappingHelper.toJson(callbackRequest))
            );
    }

    private ResultActions makeRequestContactDetailsChangeRequest(String callbackType) throws Exception {
        CaseDetails caseDetailsTemp = successfulCoreCaseDataStoreSubmitResponse();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetailsTemp);

        CaseDetails caseDetailsBefore = caseDetailsTemp.toBuilder()
            .data(caseDetailsConverter.convertToMap(ccdCase.toBuilder()
                .respondents(getUnlinkedRespondent(ccdCase))
                .build()))
            .build();

        CaseDetails caseDetails = caseDetailsBefore.toBuilder()
            .data(caseDetailsConverter.convertToMap(ccdCase.toBuilder()
                .contactChangeParty(CCDContactPartyType.CLAIMANT)
                .applicants(getApplicantWithContactDetailsModified(ccdCase))
                .build()))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.CHANGE_CONTACT_DETAILS.getValue())
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

    @NotNull
    private ImmutableList<CCDCollectionElement<CCDApplicant>> getApplicantWithContactDetailsModified(CCDCase ccdCase) {
        return ImmutableList.of(CCDCollectionElement.<CCDApplicant>builder()
            .value(ccdCase.getApplicants().get(0).getValue().toBuilder()
                .partyDetail(SampleData.getCCDPartyWithEmail("some@mail.com"))
                .build())
            .build());
    }

    @NotNull
    private ImmutableList<CCDCollectionElement<CCDRespondent>> getUnlinkedRespondent(CCDCase ccdCase) {
        return ImmutableList.of(CCDCollectionElement.<CCDRespondent>builder()
            .value(ccdCase.getRespondents().get(0).getValue().toBuilder()
                .defendantId(null)
                .build())
            .build());
    }

}
