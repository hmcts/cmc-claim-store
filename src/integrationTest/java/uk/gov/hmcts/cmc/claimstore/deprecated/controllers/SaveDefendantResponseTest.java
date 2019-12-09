package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.document.domain.Classification;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NOTIFICATION_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class SaveDefendantResponseTest extends BaseIntegrationTest {

    protected static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};

    @MockBean
    private DefendantResponseStaffNotificationHandler staffActionsHandler;

    @Captor
    private ArgumentCaptor<DefendantResponseEvent> defendantResponseEventArgument;

    private Claim claim;

    @Before
    public void setup() {
        claim = claimStore.saveClaim(SampleClaimData.builder()
            .withExternalId(UUID.randomUUID()).build(), "1", LocalDate.now());

        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail(DEFENDANT_EMAIL)
            .withRoles("letter-" + claim.getLetterHolderId(), "citizen")
            .build();

        when(userService.getUserDetails(BEARER_TOKEN)).thenReturn(userDetails);
        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, userDetails));
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).willReturn(PDF_BYTES);
        caseRepository.linkDefendant(BEARER_TOKEN);

    }

    @Test
    public void shouldReturnNewlyCreatedDefendantResponse() throws Exception {
        Response response = SampleResponse.validDefaults();

        MvcResult result = makeRequest(claim.getExternalId(), DEFENDANT_ID, response)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getResponse, Claim::getRespondedAt)
            .doesNotContainNull()
            .contains(Optional.of(response));
    }

    @Test
    public void shouldInvokeStaffActionsHandlerAfterSuccessfulSave() throws Exception {
        Response response = SampleResponse.validDefaults();

        makeRequest(claim.getExternalId(), DEFENDANT_ID, response)
            .andExpect(status().isOk());

        verify(staffActionsHandler).onDefendantResponseSubmitted(defendantResponseEventArgument.capture());

        Claim updatedClaim = claimRepository.getById(claim.getId()).orElseThrow(RuntimeException::new);
        assertThat(defendantResponseEventArgument.getValue().getClaim()).isEqualTo(updatedClaim);
    }

    @Test
    public void shouldUploadDocumentToDocumentManagementAfterSuccessfulSave() throws Exception {
        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        Response response = SampleResponse.validDefaults();
        given(documentUploadClient
            .upload(eq(BEARER_TOKEN), any(), any(), anyList(), any(Classification.class), anyList())
        ).willReturn(successfulDocumentManagementUploadResponse());

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
        InMemoryMultipartFile defendantResponseReceipt = new InMemoryMultipartFile(
            "files",
            buildResponseFileBaseName(claim.getReferenceNumber()) + ".pdf",
            MediaType.APPLICATION_PDF_VALUE,
            PDF_BYTES
        );
        makeRequest(claim.getExternalId(), DEFENDANT_ID, response)
            .andExpect(status().isOk());

        verify(documentUploadClient).upload(anyString(),
            anyString(),
            anyString(),
            anyList(),
            any(Classification.class),
            argument.capture());

        List<MultipartFile> files = argument.getValue();
        assertTrue(files.contains(defendantResponseReceipt));
    }

    @Test
    public void shouldNotReturn500WhenUploadToDocumentManagementFails() throws Exception {
        Response response = SampleResponse.validDefaults();
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), any(), any(), any()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
        MvcResult result = makeRequest(claim.getExternalId(), DEFENDANT_ID, response)
            .andExpect(status().isOk())
            .andReturn();
        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getResponse, Claim::getRespondedAt)
            .doesNotContainNull()
            .contains(Optional.of(response));
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        Response response = SampleResponse.validDefaults();

        makeRequest(claim.getExternalId(), DEFENDANT_ID, response).andExpect(status().isOk());

        verify(notificationClient, times(2))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldRetrySendNotifications() throws Exception {
        Response response = SampleResponse.validDefaults();

        given(documentUploadClient
            .upload(anyString(), anyString(), anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email1")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email2")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email3")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email4")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email5")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email6")));

        makeRequest(claim.getExternalId(), DEFENDANT_ID, response).andExpect(status().is5xxServerError());

        verify(notificationClient, atLeast(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());

        verify(appInsights).trackEvent(
            eq(NOTIFICATION_FAILURE),
            eq(REFERENCE_NUMBER),
            anyString()
        );
    }

    @Test
    public void shouldReturnInternalServerErrorWhenStaffNotificationFails() throws Exception {
        Response response = SampleResponse.validDefaults();

        doThrow(new RuntimeException()).when(staffActionsHandler).onDefendantResponseSubmitted(any());

        makeRequest(claim.getExternalId(), DEFENDANT_ID, response)
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenInvalidResponseIsSubmitted() throws Exception {

        Response invalidResponse = SampleResponse.FullDefence.builder()
            .withDefenceType(null)
            .build();

        makeRequest(claim.getExternalId(), DEFENDANT_ID, invalidResponse)
            .andExpect(status().isUnprocessableEntity());
    }

    private ResultActions makeRequest(String externalId, String defendantId, Response response) throws Exception {
        return webClient
            .perform(post("/responses/claim/" + externalId + "/defendant/" + defendantId)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                .content(jsonMapper.toJson(response))
            );
    }
}
