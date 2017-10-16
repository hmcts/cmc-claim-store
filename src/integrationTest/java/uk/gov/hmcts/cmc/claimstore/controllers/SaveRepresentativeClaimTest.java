package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.SUBMITTER_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.RESPONSE_DEADLINE;

public class SaveRepresentativeClaimTest extends BaseTest {

    private static final long CLAIM_ID = 1L;
    private static final long CLAIMANT_ID = 123L;
    private static final Long LETTER_HOLDER_ID = 1L;
    private static final Long DEFENDANT_ID = 2L;
    private static final String REFERENCE_NUMBER = "000MC001";
    private static final String PIN = "my-pin";
    private static final String EXTERNAL_ID = "external-id";
    private static final boolean DEADLINE_NOT_UPDATED = false;
    private static final String REPRESENTATIVE_EMAIL_TEMPLATE = "f2b21b9c-fc4a-4589-807b-3156dbf5bf01";

    private static final ClaimData claimData = SampleClaimData.builder()
        .withAmount(SampleAmountRange.validDefaults())
        .build();

    private static final Claim claimAfterSaving = new Claim(
        CLAIM_ID,
        CLAIMANT_ID,
        LETTER_HOLDER_ID,
        DEFENDANT_ID,
        EXTERNAL_ID,
        REFERENCE_NUMBER,
        claimData,
        NOW_IN_LOCAL_ZONE,
        ISSUE_DATE,
        RESPONSE_DEADLINE,
        DEADLINE_NOT_UPDATED,
        SUBMITTER_EMAIL,
        null, null, null ,null, null, null);

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Before
    public void setup() throws NotificationClientException {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});

        given(claimRepository.saveRepresented(anyString(), anyLong(), any(LocalDate.class),
            any(LocalDate.class), anyString(), anyString()))
            .willReturn(CLAIM_ID);

        given(claimRepository.getById(CLAIM_ID)).willReturn(Optional.of(claimAfterSaving));

        given(userService.generatePin(anyString(), anyString()))
            .willReturn(new GeneratePinResponse(PIN, LETTER_HOLDER_ID));

        given(userService.getUserDetails(anyString())).willReturn(
            SampleUserDetails.builder().withUserId(CLAIMANT_ID).withMail("claimant@email.com").build());

        given(holidaysCollection.getPublicHolidays()).willReturn(emptySet());

        given(claimRepository.getByClaimReferenceNumber(REFERENCE_NUMBER)).willReturn(Optional.of(claimAfterSaving));
    }

    @Test
    public void shouldReturnNewlyCreatedClaim() throws Exception {
        //when
        MvcResult result = postClaim(claimData)
            .andExpect(status().isOk())
            .andReturn();

        //then
        final Claim returnedClaim = jsonMapper.fromJson(result.getResponse().getContentAsString(), Claim.class);
        assertThat(returnedClaim.getClaimData()).isEqualToComparingFieldByField(claimAfterSaving.getClaimData());
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(null);

        final String referenceNumber = "representative-issue-notification-" + REFERENCE_NUMBER;
        postClaim(claimData)
            .andExpect(status().isOk())
            .andReturn();

        verify(notificationClient).sendEmail(eq(REPRESENTATIVE_EMAIL_TEMPLATE), anyString(),
            anyMap(), eq(referenceNumber));
    }

    @Test
    public void shouldSendStaffNotificationsForLegalClaimIssuedEvent() throws Exception {

        postClaim(claimData)
            .andExpect(status().isOk())
            .andReturn();

        verify(emailService).sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getTo()).isEqualTo("recipient@example.com");
        assertThat(emailDataArgument.getValue().getSubject()).isEqualTo("Claim form " + REFERENCE_NUMBER);
        assertThat(emailDataArgument.getValue().getMessage()).isEqualTo("Please find attached claim.");
        final List<EmailAttachment> attachments = emailDataArgument.getValue().getAttachments();
        assertThat(attachments).hasSize(1);
        assertThat(attachments.get(0).getFilename()).isEqualTo("000MC001-sealed-claim.pdf");
    }

    @Test
    public void shouldFailForInvalidClaimApplication() throws Exception {
        ClaimData withValidationFailures = SampleClaimData.builder()
            .clearClaimants()
            .addClaimant(SampleParty.builder()
                .withName(null)
                .individual())
            .withDefendant(SampleTheirDetails.builder()
                .withAddress(null)
                .individualDetails())
            .withAmount(SampleAmountRange.validDefaults())
            .build();

        MvcResult result = postClaim(withValidationFailures)
            .andExpect(status().isBadRequest())
            .andReturn();

        List<String> errors = extractErrors(result);
        assertThat(errors)
            .hasSize(2)
            .contains("claimants[0].name : may not be empty")
            .contains("defendants[0].address : may not be null");
    }

    @Test
    public void shouldFailForInvalidClaimInterestDate() throws Exception {
        ClaimData withInvalidInterestDate = SampleClaimData.builder()
            .withInterestDate(SampleInterestDate.builder()
                .withType(null)
                .withDate(null)
                .withReason(null)
                .build())
            .withAmount(SampleAmountRange.validDefaults())
            .build();

        MvcResult result = postClaim(withInvalidInterestDate)
            .andExpect(status().isBadRequest())
            .andReturn();

        List<String> errors = extractErrors(result);
        assertThat(errors)
            .hasSize(2)
            .contains("type : may not be null")
            .contains("interestDate : reason : may not be empty");
    }

    @Test
    public void shouldBeSuccessfulWhenClaimInterestTypeIsNoInterest() throws Exception {
        MvcResult result = postClaim(SampleClaimData.noInterest())
            .andExpect(status().isOk())
            .andReturn();

        final Claim returnedClaim = jsonMapper.fromJson(result.getResponse().getContentAsString(), Claim.class);
        assertThat(returnedClaim).isEqualTo(claimAfterSaving);
    }

    private ResultActions postClaim(ClaimData claimData) throws Exception {
        return webClient
            .perform(post("/claims/" + (Long) 123L)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(claimData))
            );
    }
}
