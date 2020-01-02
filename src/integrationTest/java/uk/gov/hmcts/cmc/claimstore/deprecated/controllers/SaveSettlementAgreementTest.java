package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.document.domain.Classification;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_REJECTED_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_SIGNED_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NOTIFICATION_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.AgreementCounterSigned.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.AgreementCounterSigned.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSettlementReachedFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.domain.models.offers.MadeBy.DEFENDANT;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class SaveSettlementAgreementTest extends BaseIntegrationTest {

    private Claim claim;

    @Before
    public void setUp() {
        claim = claimStore.saveClaim(SampleClaimData.builder()
            .withExternalId(UUID.randomUUID()).build(), SUBMITTER_ID, LocalDate.now());

        claimStore.saveResponse(claim, SampleResponse.FullAdmission.builder().build());

        UserDetails defendantDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail(DEFENDANT_EMAIL)
            .withRoles("letter-" + claim.getLetterHolderId())
            .build();

        UserDetails claimantDetails = SampleUserDetails.builder()
            .withUserId(SUBMITTER_ID)
            .build();

        when(userService.getUserDetails(BEARER_TOKEN)).thenReturn(defendantDetails);
        when(userService.getUserDetails(AUTHORISATION_TOKEN)).thenReturn(claimantDetails);
        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, defendantDetails));
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(new User(AUTHORISATION_TOKEN, claimantDetails));
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).willReturn(PDF_BYTES);
        caseRepository.linkDefendant(BEARER_TOKEN);
        caseRepository.saveClaimantResponse(claim,
            new SampleClaimantResponse.ClaimantResponseAcceptation()
                .buildAcceptationIssueSettlementWithClaimantPaymentIntention(),
            BEARER_TOKEN);

        Settlement settlement = new Settlement();
        settlement.makeOffer(new Offer("offer", LocalDate.now(), null), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);
        caseRepository.updateSettlement(claim, settlement, BEARER_TOKEN, AGREEMENT_SIGNED_BY_CLAIMANT);
    }

    @Test
    public void shouldRejectSettlementAgreement() throws Exception {
        makeRequest(claim.getExternalId(), "reject").andExpect(status().isCreated());

        Claim claimWithSettlementAgreement = claimStore.getClaimByExternalId(claim.getExternalId());

        Settlement settlement = claimWithSettlementAgreement.getSettlement().orElseThrow(AssertionError::new);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.REJECTION);
        assertThat(settlement.getLastStatement().getMadeBy()).isEqualTo(MadeBy.DEFENDANT);
    }

    @Test
    public void shouldCountersignSettlementAgreement() throws Exception {
        makeRequest(claim.getExternalId(), "countersign").andExpect(status().isCreated());

        Claim claimWithSettlementAgreement = claimStore.getClaimByExternalId(claim.getExternalId());

        Settlement settlement = claimWithSettlementAgreement.getSettlement().orElseThrow(AssertionError::new);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.COUNTERSIGNATURE);
        assertThat(settlement.getLastStatement().getMadeBy()).isEqualTo(MadeBy.DEFENDANT);
    }

    @Test
    public void shouldUploadDocumentToDocumentManagementAfterCountersignSettlementAgreement() throws Exception {
        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        given(documentUploadClient
            .upload(eq(AUTHORISATION_TOKEN), any(), any(), anyList(), any(Classification.class), any())
        ).willReturn(successfulDocumentManagementUploadResponse());

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
        makeRequest(claim.getExternalId(), "countersign").andExpect(status().isCreated());
        Claim claimWithSettlementAgreement = claimStore.getClaimByExternalId(claim.getExternalId());
        InMemoryMultipartFile settlementAgreement = new InMemoryMultipartFile(
            "files",
            buildSettlementReachedFileBaseName(claimWithSettlementAgreement.getReferenceNumber()) + ".pdf",
            MediaType.APPLICATION_PDF_VALUE,
            PDF_BYTES
        );
        Settlement settlement = claimWithSettlementAgreement.getSettlement().orElseThrow(AssertionError::new);

        verify(documentUploadClient).upload(anyString(),
            anyString(),
            anyString(),
            anyList(),
            any(Classification.class),
            argument.capture());

        List<MultipartFile> files = argument.getValue();
        assertTrue(files.contains(settlementAgreement));
        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.COUNTERSIGNATURE);
        assertThat(settlement.getLastStatement().getMadeBy()).isEqualTo(MadeBy.DEFENDANT);
    }

    @Test
    public void shouldNotReturn500HttpStatusAfterCountersignSettlementAgreementWhenUploadDocumentFails()
        throws Exception {
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), any(), any(), any()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
        makeRequest(claim.getExternalId(), "countersign").andExpect(status().isCreated());
        Claim claimWithSettlementAgreement = claimStore.getClaimByExternalId(claim.getExternalId());
        Settlement settlement = claimWithSettlementAgreement.getSettlement().orElseThrow(AssertionError::new);
        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.COUNTERSIGNATURE);
        assertThat(settlement.getLastStatement().getMadeBy()).isEqualTo(MadeBy.DEFENDANT);
    }

    @Test
    public void shouldReturn400WhenClaimIsNotInStateForSettlementAgreementRejection() throws Exception {
        Settlement settlement = new Settlement();
        caseRepository.updateSettlement(claim, settlement, BEARER_TOKEN, AGREEMENT_REJECTED_BY_DEFENDANT);

        makeRequest(claim.getExternalId(), "reject").andExpect(status().isBadRequest());
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        makeRequest(claim.getExternalId(), "countersign").andExpect(status().isCreated());
        String referenceNumber = claim.getReferenceNumber();

        verify(notificationClient)
            .sendEmail(anyString(), anyString(), anyMap(), eq(referenceForClaimant(referenceNumber, DEFENDANT.name())));

        verify(notificationClient).sendEmail(
            anyString(),
            anyString(),
            anyMap(),
            eq(referenceForDefendant(referenceNumber, DEFENDANT.name()))
        );
    }

    @Test
    public void shouldRetrySendNotifications() throws Exception {
        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("invalid claimant email1")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid claimant email2")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid claimant email3")));

        given(documentUploadClient.upload(anyString(), anyString(),
            anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());

        makeRequest(claim.getExternalId(), "countersign").andExpect(status().is5xxServerError());
        String referenceNumber = claim.getReferenceNumber();

        verify(notificationClient, atLeast(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());

        verify(appInsights).trackEvent(eq(NOTIFICATION_FAILURE), eq(REFERENCE_NUMBER), anyString());
    }

    private ResultActions makeRequest(String externalId, String action) throws Exception {
        String path = String.format("/claims/%s/settlement-agreement/%s", externalId, action);

        return webClient
            .perform(post(path)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            );
    }
}
