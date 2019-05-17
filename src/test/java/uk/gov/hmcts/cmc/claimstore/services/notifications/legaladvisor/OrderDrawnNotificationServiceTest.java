package uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor;


import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.notifications.BaseNotificationServiceTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;

import java.util.Collections;
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
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        Map<String, String> expectedParams = ImmutableMap.of(
            "claimReferenceNumber", ccdCase.getPreviousServiceCaseReference(),
            "claimantName", ccdCase.getApplicants().get(0).getValue().getPartyName(),
            "frontendBaseUrl", FRONTEND_BASE_URL
        );
        service.notifyClaimant(ccdCase);
        verify(notificationService).sendMail(
            eq(ccdCase.getApplicants().get(0).getValue().getPartyDetail().getEmailAddress()),
            eq("claimantTemplate"),
            eq(expectedParams),
            eq(String.format(reference, "claimant", ccdCase.getPreviousServiceCaseReference())));
    }

    @Test
    public void shouldSendEmailToDefendantUsingPredefinedTemplate() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        Map<String, String> expectedParams = ImmutableMap.of(
            "claimReferenceNumber", ccdCase.getPreviousServiceCaseReference(),
            "defendantName", ccdCase.getRespondents().get(0).getValue().getPartyName(),
            "frontendBaseUrl", FRONTEND_BASE_URL
        );
        service.notifyDefendant(ccdCase);
        verify(notificationService).sendMail(
            eq(ccdCase.getRespondents().get(0).getValue().getPartyDetail().getEmailAddress()),
            eq("defendantTemplate"),
            eq(expectedParams),
            eq(String.format(reference, "defendant", ccdCase.getPreviousServiceCaseReference())));
    }
}
