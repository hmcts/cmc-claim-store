package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.TransferContent;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.SUBMITTER_EMAIL;

@ExtendWith(MockitoExtension.class)
class TransferCaseNotificationsServiceTest {

    private static final String CASE_TRANSFERRED_TEMPLATE = "CASE_TRANSFERRED_TEMPLATE";
    private static final String TRANSFER_COURT_NAME = "Bristol";
    private static final String TRANSFER_CCBC = "County Court Business Centre";

    @InjectMocks
    private TransferCaseNotificationsService transferCaseNotificationsService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Captor
    ArgumentCaptor<String> emailCaptor;

    @Captor
    ArgumentCaptor<String> valueCaptor;

    @Captor
    ArgumentCaptor<Map<String, String>> aggregatedParamsCaptor;

    private Claim claim;

    private CCDCase ccdCase;

    @BeforeEach
    public void beforeEach() {

        ReflectionTestUtils.setField(transferCaseNotificationsService,
            "caseTransferToCcbcForClaimantTemplate", CASE_TRANSFERRED_TEMPLATE);

        ReflectionTestUtils.setField(transferCaseNotificationsService,
            "caseTransferToCourtTemplate", CASE_TRANSFERRED_TEMPLATE);

        ReflectionTestUtils.setField(transferCaseNotificationsService,
            "frontendBaseUrl", FRONTEND_BASE_URL);

        claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withTransferContent(TransferContent.builder()
                .hearingCourtName(TRANSFER_COURT_NAME)
                .build())
            .build();

        ccdCase = SampleData.getCCDLegalCase();
    }

    @Test
    void shouldSendTransferToCourtEmailToClaimant() {
        transferCaseNotificationsService.sendTransferToCourtEmail(ccdCase, claim);
        String partyName = claim.getClaimData().getClaimant().getName();
        thenEmailSent(SUBMITTER_EMAIL, "to-claimant-case-transferred-000MC001", partyName, TRANSFER_COURT_NAME);
    }

    @Test
    void shouldSendTransferToCcbcEmailToClaimant() {
        transferCaseNotificationsService.sendTransferToCcbcEmail(ccdCase, claim);
        String partyName = claim.getClaimData().getClaimant().getName();
        thenEmailSent(SUBMITTER_EMAIL, "to-claimant-case-transferred-000MC001", partyName, TRANSFER_CCBC);
    }

    @Test
    void shouldSendTransferEmailNotificationToBothClaimantAndDefendant() {
        transferCaseNotificationsService.sendTransferToCcbcEmail(getCCDCaseWithLinkedRespondent(), claim);
        verify(notificationService, times(2)).sendMail(
            emailCaptor.capture(),
            valueCaptor.capture(),
            aggregatedParamsCaptor.capture(),
            valueCaptor.capture());
        assertEquals(DEFENDANT_EMAIL, emailCaptor.getAllValues().get(1));
        assertTrue(aggregatedParamsCaptor.getAllValues().get(0).containsKey(CLAIMANT_NAME));
        assertTrue(aggregatedParamsCaptor.getAllValues().get(1).containsKey(DEFENDANT_NAME));
    }

    public static CCDCase getCCDCaseWithLinkedRespondent() {
        CCDRespondent respondent = CCDRespondent.builder().defendantId("123").build();
        List<CCDCollectionElement<CCDRespondent>> respondents
            = singletonList(CCDCollectionElement.<CCDRespondent>builder().value(respondent).build());
        return CCDCase.builder().respondents(respondents).build();
    }

    private void thenEmailSent(String recipientEmail, String reference, String partyName, String courtName) {

        Map<String, String> expectedParams = Map.of(
            "claimReferenceNumber", claim.getReferenceNumber(),
            "partyName", partyName,
            "claimantName", partyName,
            "frontendBaseUrl", FRONTEND_BASE_URL,
            "externalId", claim.getExternalId(),
            "courtName", courtName
        );

        verify(notificationService).sendMail(
            eq(recipientEmail),
            eq(CASE_TRANSFERRED_TEMPLATE),
            eq(expectedParams),
            eq(reference));
    }
}
