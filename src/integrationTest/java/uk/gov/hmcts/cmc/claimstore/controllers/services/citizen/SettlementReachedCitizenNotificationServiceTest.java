package uk.gov.hmcts.cmc.claimstore.controllers.services.citizen;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCounterSignedCitizenActionsHandler;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SettlementReachedCitizenNotificationServiceTest extends MockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private AgreementCounterSignedCitizenActionsHandler handler;

    @Captor
    private ArgumentCaptor<String> senderArgument;

    @Captor
    private ArgumentCaptor<String> templateArgument;

    @Captor
    private ArgumentCaptor<String> referenceArgument;

    @Captor
    private ArgumentCaptor<Map<String, String>> emailDataArgument;

    @Autowired
    private NotificationsProperties notificationsProperties;

    private Claim claim;
    private AgreementCountersignedEvent event;

    @Before
    public void setup() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.validDefaults(), MadeBy.DEFENDANT);
        settlement.accept(MadeBy.CLAIMANT);
        settlement.countersign(MadeBy.DEFENDANT);

        claim = SampleClaim
            .builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withSettlementReachedAt(LocalDateTime.now())
            .withResponse(SampleResponse.validDefaults())
            .withSettlement(settlement)
            .build();


        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test
    public void shouldSendEmailToDefendantWithExpectedTemplateAndReferenceWhenCounterSignedByDefendant()
        throws NotificationClientException {
        event = new AgreementCountersignedEvent(claim, MadeBy.DEFENDANT);
        handler.sendNotificationToOfferOriginator(event);

        verify(notificationClient).sendEmail(templateArgument.capture(), senderArgument.capture(),
            emailDataArgument.capture(), referenceArgument.capture());

        assertThat(templateArgument.getValue()).isEqualTo("9d1ddac9-d6a7-41f3-bfd4-dcfbcb61dcf1");

        assertThat(referenceArgument.getValue())
            .isEqualTo("to-claimant-agreement-counter-signed-by-defendant-notification-000CM001");

        Map<String, String> emailData = new HashMap<>();
        emailData.put("claimReferenceNumber", "000CM001");
        emailData.put("counterSigningParty", claim.getClaimData().getDefendant().getName());
        emailData.put("frontendBaseUrl", "https://civil-money-claims.co.uk");

        assertThat(emailDataArgument.getValue()).isEqualTo(emailData);
    }


    @Test
    public void shouldSendEmailToClaimantWithExpectedTemplateAndReferenceWhenCounterSignedByDefendant()
        throws NotificationClientException {
        event = new AgreementCountersignedEvent(claim, MadeBy.DEFENDANT);
        handler.sendNotificationToOtherParty(event);

        verify(notificationClient).sendEmail(templateArgument.capture(), senderArgument.capture(),
            emailDataArgument.capture(), referenceArgument.capture());

        assertThat(templateArgument.getValue()).isEqualTo("cfde3889-e202-4d70-bc64-f54048616be3");

        assertThat(referenceArgument.getValue())
            .isEqualTo("to-claimant-agreement-counter-signed-by-defendant-notification-000CM001");

        Map<String, String> emailData = new HashMap<>();
        emailData.put("claimReferenceNumber", "000CM001");
        emailData.put("counterSigningParty", claim.getClaimData().getDefendant().getName());
        emailData.put("frontendBaseUrl", "https://civil-money-claims.co.uk");

        assertThat(emailDataArgument.getValue()).isEqualTo(emailData);
    }


    @Test
    public void shouldSendEmailToClaimantWithExpectedTemplateAndReferenceWhenCounterSignedByClaimant()
        throws NotificationClientException {
        event = new AgreementCountersignedEvent(claim, MadeBy.CLAIMANT);
        handler.sendNotificationToOfferOriginator(event);

        verify(notificationClient).sendEmail(templateArgument.capture(), senderArgument.capture(),
            emailDataArgument.capture(), referenceArgument.capture());

        assertThat(templateArgument.getValue()).isEqualTo("9d1ddac9-d6a7-41f3-bfd4-dcfbcb61dcf1");

        assertThat(referenceArgument.getValue())
            .isEqualTo("to-defendant-agreement-counter-signed-by-claimant-notification-000CM001");

        Map<String, String> emailData = new HashMap<>();
        emailData.put("claimReferenceNumber", "000CM001");
        emailData.put("counterSigningParty", claim.getClaimData().getClaimant().getName());
        emailData.put("frontendBaseUrl", "https://civil-money-claims.co.uk");

        assertThat(emailDataArgument.getValue()).isEqualTo(emailData);
    }


    @Test
    public void shouldSendEmailToDefendantWithExpectedTemplateAndReferenceWhenCounterSignedByClaimant()
        throws NotificationClientException {
        event = new AgreementCountersignedEvent(claim, MadeBy.CLAIMANT);
        handler.sendNotificationToOtherParty(event);

        verify(notificationClient).sendEmail(templateArgument.capture(), senderArgument.capture(),
            emailDataArgument.capture(), referenceArgument.capture());

        assertThat(templateArgument.getValue()).isEqualTo("cfde3889-e202-4d70-bc64-f54048616be3");

        assertThat(referenceArgument.getValue())
            .isEqualTo("to-defendant-agreement-counter-signed-by-claimant-notification-000CM001");

        Map<String, String> emailData = new HashMap<>();
        emailData.put("claimReferenceNumber", "000CM001");
        emailData.put("counterSigningParty", claim.getClaimData().getClaimant().getName());
        emailData.put("frontendBaseUrl", "https://civil-money-claims.co.uk");

        assertThat(emailDataArgument.getValue()).isEqualTo(emailData);
    }
}
