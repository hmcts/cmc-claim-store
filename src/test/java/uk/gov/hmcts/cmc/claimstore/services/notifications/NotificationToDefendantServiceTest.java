package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationToDefendantServiceTest extends BaseNotificationServiceTest {

    private static final String REFERENCE = "to-defendant-claimant’s-response-submitted-notification-000CM001";
    private static final String CLAIMANT_RESPONSE_TEMPLATE = "templateId";
    private static final String DEFENDANT_EMAIL = "defendant@email.com";
    private static final String INTERLOCUTORY_JUDGEMENT_TEMPLATE = "interlocutoryJudgementTemplateId";
    private static final String CLAIMANT_REJECT_COMPANY_OR_ORG_RESPONSE_TEMPLATE = "claimantRejectsPlanTemplateId";

    private NotificationToDefendantService service;
    private Claim claim;

    @Before
    public void beforeEachTest() {
        service = new NotificationToDefendantService(notificationClient, properties);
        claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .build();
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(properties.getTemplates()).thenReturn(templates);
        when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
    }

    @Test(expected = NotificationException.class)
    public void shouldThrowNotificationExceptionWhenClientThrowsNotificationClientException() throws Exception {
        when(emailTemplates.getResponseByClaimantEmailToDefendant()).thenReturn(CLAIMANT_RESPONSE_TEMPLATE);
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(mock(NotificationClientException.class));

        service.notifyDefendant(claim);
    }

    @Test
    public void shouldSendEmailUsingPredefinedTemplate() throws Exception {
        when(emailTemplates.getResponseByClaimantEmailToDefendant()).thenReturn(CLAIMANT_RESPONSE_TEMPLATE);
        service.notifyDefendant(claim);

        verify(notificationClient).sendEmail(
            eq(CLAIMANT_RESPONSE_TEMPLATE),
            eq(DEFENDANT_EMAIL),
            anyMap(),
            eq(REFERENCE)
        );
    }

    //WIP
    @Test
    public void shouldSendEmailWhenClaimantRejectCompanyOrOrgRepaymentPlan() throws Exception {
        when(emailTemplates.getClaimantRejectionResponseToCompanyOrOrganisation())
            .thenReturn(CLAIMANT_REJECT_COMPANY_OR_ORG_RESPONSE_TEMPLATE);
        service.notifyDefendant(claim);

        verify(notificationClient).sendEmail(
            eq(CLAIMANT_RESPONSE_TEMPLATE),
            eq(DEFENDANT_EMAIL),
            anyMap(),
            eq(REFERENCE)
        );
    }

    @Test
    public void shouldSendEmailUsingInterlocutoryCCJTemplate() throws Exception {
        when(emailTemplates.getClaimantRequestedInterlocutoryJudgement()).thenReturn(INTERLOCUTORY_JUDGEMENT_TEMPLATE);
        service.notifyDefendantWhenInterlocutoryJudgementRequested(claim);

        verify(notificationClient).sendEmail(
            eq(INTERLOCUTORY_JUDGEMENT_TEMPLATE),
            eq(DEFENDANT_EMAIL),
            anyMap(),
            eq(REFERENCE)
        );
    }
}
