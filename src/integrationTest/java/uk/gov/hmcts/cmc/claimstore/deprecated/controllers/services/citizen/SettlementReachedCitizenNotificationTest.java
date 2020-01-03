package uk.gov.hmcts.cmc.claimstore.deprecated.controllers.services.citizen;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseOfferTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.cmc.domain.models.offers.MadeBy.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.offers.MadeBy.DEFENDANT;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class SettlementReachedCitizenNotificationTest extends BaseOfferTest {

    private static final String OFFER_COUNTER_SIGNED_EMAIL_TO_ORIGINATOR = "9d1ddac9-d6a7-41f3-bfd4-dcfbcb61dcf1";
    private static final String OFFER_COUNTER_SIGNED_EMAIL_TO_OTHER_PARTY = "cfde3889-e202-4d70-bc64-f54048616be3";

    private static final String FRONTEND_BASE_URL = "https://civil-money-claims.co.uk";

    @Test
    public void verifyNotificationsWhenCounterSignedByDefendant() throws Exception {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(PDF_CONTENT);

        claimantRequestFor("accept");
        defendantRequestFor("countersign");

        verify(notificationClient).sendEmail(
            OFFER_COUNTER_SIGNED_EMAIL_TO_ORIGINATOR,
            claim.getDefendantEmail(),
            counterSignedEmailDataFor(claim.getClaimData().getDefendant().getName()),
            NotificationReferenceBuilder.AgreementCounterSigned
                .referenceForDefendant(claim.getReferenceNumber(), DEFENDANT.name())
        );

        verify(notificationClient).sendEmail(
            OFFER_COUNTER_SIGNED_EMAIL_TO_OTHER_PARTY,
            claim.getSubmitterEmail(),
            counterSignedEmailDataFor(claim.getClaimData().getDefendant().getName()),
            NotificationReferenceBuilder.AgreementCounterSigned
                .referenceForClaimant(claim.getReferenceNumber(), DEFENDANT.name())
        );
    }

    private Map<String, String> counterSignedEmailDataFor(String counterSigningPartyName) {
        Map<String, String> emailData = new HashMap<>();
        emailData.put(NotificationTemplateParameters.CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        emailData.put(NotificationTemplateParameters.DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        emailData.put(NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        emailData.put(NotificationTemplateParameters.FRONTEND_BASE_URL, FRONTEND_BASE_URL);
        return emailData;
    }

    @Test
    public void verifyNotificationsWhenCounterSignedByClaimant() throws Exception {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(PDF_CONTENT);

        claimantRequestFor("reject");
        prepareClaimantOffer();
        defendantRequestFor("accept");
        claimantRequestFor("countersign");

        verify(notificationClient).sendEmail(
            OFFER_COUNTER_SIGNED_EMAIL_TO_ORIGINATOR,
            claim.getSubmitterEmail(),
            counterSignedEmailDataFor(claim.getClaimData().getClaimant().getName()),
            NotificationReferenceBuilder.AgreementCounterSigned
                .referenceForClaimant(claim.getReferenceNumber(), CLAIMANT.name())
        );

        verify(notificationClient).sendEmail(
            OFFER_COUNTER_SIGNED_EMAIL_TO_OTHER_PARTY,
            claim.getDefendantEmail(),
            counterSignedEmailDataFor(claim.getClaimData().getClaimant().getName()),
            NotificationReferenceBuilder.AgreementCounterSigned
                .referenceForDefendant(claim.getReferenceNumber(), CLAIMANT.name())
        );
    }

    private void claimantRequestFor(String endpoint) throws Exception {
        webClient.perform(
            post(format("/claims/%s/offers/%s/%s", claim.getExternalId(), MadeBy.CLAIMANT.name(), endpoint))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, CLAIMANT_AUTH_TOKEN)
        );
    }

    private void defendantRequestFor(String endpoint) throws Exception {
        webClient.perform(
            post(format("/claims/%s/offers/%s/%s", claim.getExternalId(), MadeBy.DEFENDANT.name(), endpoint))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, DEFENDANT_AUTH_TOKEN)
        );
    }

}
