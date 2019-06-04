package uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.notifications.BaseNotificationServiceTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderDrawnNotificationServiceTest extends BaseNotificationServiceTest {
    private final String reference = "to-%s-legal-order-drawn-%s";

    @Mock
    protected NotificationService notificationService;

    private OrderDrawnNotificationService service;

    @Before
    public void beforeEachTest() {
        service = new OrderDrawnNotificationService(notificationService, properties);
        when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        when(properties.getTemplates()).thenReturn(templates);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getClaimantLegalOrderDrawn()).thenReturn("claimantTemplate");
        when(emailTemplates.getDefendantLegalOrderDrawn()).thenReturn("defendantTemplate");
    }

    @Test
    public void shouldSendEmailToClaimantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().build();
        Map<String, String> expectedParams = ImmutableMap.of(
            "claimReferenceNumber", claim.getReferenceNumber(),
            "claimantName", claim.getClaimData().getClaimant().getName(),
            "frontendBaseUrl", FRONTEND_BASE_URL
        );
        service.notifyClaimant(claim);
        verify(notificationService).sendMail(
            eq(SampleClaim.SUBMITTER_EMAIL),
            eq("claimantTemplate"),
            eq(expectedParams),
            eq(String.format(reference, "claimant", claim.getReferenceNumber())));
    }

    @Test
    public void shouldSendEmailToDefendantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().build();
        Map<String, String> expectedParams = ImmutableMap.of(
            "claimReferenceNumber", claim.getReferenceNumber(),
            "defendantName", claim.getClaimData().getDefendant().getName(),
            "frontendBaseUrl", FRONTEND_BASE_URL
        );
        service.notifyDefendant(claim);
        verify(notificationService).sendMail(
            eq(SampleTheirDetails.DEFENDANT_EMAIL),
            eq("defendantTemplate"),
            eq(expectedParams),
            eq(String.format(reference, "defendant", claim.getReferenceNumber())));
    }
}
