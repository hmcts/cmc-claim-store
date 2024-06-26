package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.ccd.domain.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimForHwF;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_INFO_REQUIRED_FOR_HWF;

@ExtendWith(SpringExtension.class)
public class HwfClaimNotificationServiceTest extends BaseNotificationServiceTest {

    private final String reference = "hwf-claimant-notification-" + claim.getReferenceNumber();
    private HwfClaimNotificationService service;

    @BeforeEach
    public void beforeEachTest() {
        claim = SampleClaimForHwF.getDefault().toBuilder().respondedAt(LocalDateTime.now())
            .lastEventTriggeredForHwfCase(MORE_INFO_REQUIRED_FOR_HWF.getValue()).build();
        service = new HwfClaimNotificationService(notificationClient, properties, appInsights);
        Mockito.when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        Mockito.when(properties.getRespondToClaimUrl()).thenReturn(RESPOND_TO_CLAIM_URL);
    }

    @Test
    public void emailClaimantShouldThrowRuntimeExceptionWhenNotificationClientThrows() throws Exception {
        Mockito.when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(Mockito.mock(NotificationClientException.class));

        assertThrows(NotificationException.class, () -> {
            service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);
        });

    }

    @Test
    public void emailClaimantShouldSendEmailUsingPredefinedTemplate() throws Exception {
        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(
            eq(HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE), anyString(), anyMap(), anyString());
    }

    @Test
    public void emailClaimantShouldSendToClaimantEmail() throws Exception {
        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(
            anyString(), eq(USER_EMAIL), anyMap(), anyString());
    }

    @Test
    public void emailClaimantShouldPassClaimReferenceNumberInTemplateParameters() throws Exception {
        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(
            eq(HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    @Test
    public void emailClaimantShouldPassNameInTemplateParameters() throws Exception {
        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(
            eq(HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        String name = claim.getClaimData().getClaimant().getName();

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.CLAIMANT_NAME, name);
    }

    @Test
    public void emailClaimantShouldPassNameInTemplateParametersForMoreInfo() throws Exception {
        //    HwFMoreInfoRequiredDocuments
        ClaimData claimData = claim.getClaimData();
        ClaimData updatedClaimData = claimData.toBuilder()
            .hwfMoreInfoNeededDocuments(Arrays.asList(HwFMoreInfoRequiredDocuments.ANY_OTHER_INCOME.name())).build();
        Claim claimLocal = SampleClaimForHwF.getDefault().toBuilder().respondedAt(LocalDateTime.now())
            .lastEventTriggeredForHwfCase(MORE_INFO_REQUIRED_FOR_HWF.getValue())
            .claimData(updatedClaimData)
            .build();

        service.sendMail(claimLocal, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(
            eq(HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        String name = claimLocal.getClaimData().getClaimant().getName();

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.CLAIMANT_NAME, name);
    }

    @Test
    public void emailClaimantShouldUseClaimReferenceNumberForNotificationReference() throws Exception {
        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(anyString(), anyString(),
            anyMap(), eq(reference));
    }

    @Test
    public void recoveryShouldNotLogPII() {
        NotificationException exception = new NotificationException("expected exception");
        try {
            service.logNotificationFailure(exception, "reference");
            Assertions.fail("Expected a NotificationException to be thrown");
        } catch (NotificationException expected) {
            assertWasLogged("Failure: failed to send notification (reference) due to expected exception");
        }
    }
}
