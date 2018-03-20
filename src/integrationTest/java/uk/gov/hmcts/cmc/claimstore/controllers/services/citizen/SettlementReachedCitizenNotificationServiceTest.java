package uk.gov.hmcts.cmc.claimstore.controllers.services.citizen;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseOfferTest;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.CLAIM_REFERENCE_PATTERN;

public class SettlementReachedCitizenNotificationServiceTest extends BaseOfferTest {

    private static final String CLAIM_REFERENCE_NUMBER = CLAIM_REFERENCE_PATTERN.replace("^", "");
    private static final String FRONTEND_BASE_URL = "https://civil-money-claims.co.uk";
    private static final String OFFER_COUNTER_SIGNED_EMAIL_BY_ORIGINATOR = "9d1ddac9-d6a7-41f3-bfd4-dcfbcb61dcf1";
    private static final String OFFER_COUNTERSIGNED_EMAIL_BY_OTHER_PARTY = "cfde3889-e202-4d70-bc64-f54048616be3";
    private static final String COUNTER_SIGNING_PARTY_KEY = "counterSigningParty";
    private static final String FRONTEND_BASE_URL_KEY = "frontendBaseUrl";

    @Captor
    private ArgumentCaptor<String> senderArgument;

    @Captor
    private ArgumentCaptor<String> templateArgument;

    @Captor
    private ArgumentCaptor<String> referenceArgument;

    @Captor
    private ArgumentCaptor<Map<String, String>> emailDataArgument;

    @Test
    public void verifyNotificationsWhenCounterSignedByDefendant() throws Exception {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(PDF_CONTENT);

        claimantRequestFor("accept");
        defendantRequestFor("countersign");

        verify(notificationClient, atLeast(6))
            .sendEmail(templateArgument.capture(), senderArgument.capture(), emailDataArgument.capture(),
                referenceArgument.capture());

        verifyConfirmationToDefendantOnDefendantCounterSign();
        verifyNotificationToClaimantOnDefendantCounterSign();

    }

    private void verifyConfirmationToDefendantOnDefendantCounterSign() {
        assertThat(templateArgument.getValue()).isEqualTo(OFFER_COUNTER_SIGNED_EMAIL_BY_ORIGINATOR);

        assertThat(referenceArgument.getValue())
            .matches("to-defendant-agreement-counter-signed-by-defendant-notification-"
                + CLAIM_REFERENCE_NUMBER
            );

        Map<String, String> emailData = new HashMap<>();
        emailData.put(COUNTER_SIGNING_PARTY_KEY, claim.getClaimData().getDefendant().getName());
        emailData.put(FRONTEND_BASE_URL_KEY, FRONTEND_BASE_URL);

        verifyEmailData(emailDataArgument.getValue(), emailData);

        assertThat(senderArgument.getValue())
            .isEqualTo(claim.getClaimData().getDefendant().getEmail().orElse(null));
    }

    private void verifyNotificationToClaimantOnDefendantCounterSign() {
        assertThat(templateArgument.getAllValues().get(4)).isEqualTo(OFFER_COUNTERSIGNED_EMAIL_BY_OTHER_PARTY);

        assertThat(referenceArgument.getAllValues().get(4))
            .matches("to-claimant-agreement-counter-signed-by-defendant-notification-"
                + CLAIM_REFERENCE_NUMBER
            );

        Map<String, String> emailData = new HashMap<>();
        emailData.put(COUNTER_SIGNING_PARTY_KEY, claim.getClaimData().getDefendant().getName());
        emailData.put(FRONTEND_BASE_URL_KEY, FRONTEND_BASE_URL);

        verifyEmailData(emailDataArgument.getAllValues().get(4), emailData);
        assertThat(senderArgument.getAllValues().get(4)).isEqualTo(claim.getSubmitterEmail());
    }

    @Test
    public void verifyNotificationsWhenCounterSignedByClaimant() throws Exception {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(PDF_CONTENT);

        claimantRequestFor("reject");
        prepareClaimantOffer();
        defendantRequestFor("accept");
        claimantRequestFor("countersign");

        verify(notificationClient, atLeast(10))
            .sendEmail(templateArgument.capture(), senderArgument.capture(), emailDataArgument.capture(),
                referenceArgument.capture());

        verifyConfirmationToClaimantOnClaimantCounterSign();
        verifyNotificationToDefendantOnClaimantCounterSign();
    }

    private void verifyConfirmationToClaimantOnClaimantCounterSign() {
        assertThat(templateArgument.getValue()).isEqualTo(OFFER_COUNTER_SIGNED_EMAIL_BY_ORIGINATOR);

        assertThat(referenceArgument.getValue())
            .matches("to-claimant-agreement-counter-signed-by-claimant-notification-"
                + CLAIM_REFERENCE_NUMBER);

        Map<String, String> emailData = new HashMap<>();
        emailData.put(COUNTER_SIGNING_PARTY_KEY, claim.getClaimData().getClaimant().getName());
        emailData.put(FRONTEND_BASE_URL_KEY, FRONTEND_BASE_URL);

        verifyEmailData(emailDataArgument.getValue(), emailData);
        assertThat(senderArgument.getValue()).isEqualTo(claim.getSubmitterEmail());
    }

    private void verifyEmailData(Map<String, String> output, Map<String, String> expected) {
        output.entrySet().stream()
            .filter(e -> !e.getKey().equals("claimReferenceNumber"))
            .forEach(e -> assertThat(output.get(e.getKey())).isEqualTo(expected.get(e.getKey())));

        assertThat(output.get("claimReferenceNumber")).matches(CLAIM_REFERENCE_PATTERN);

    }

    private void verifyNotificationToDefendantOnClaimantCounterSign() {
        assertThat(templateArgument.getAllValues().get(8)).isEqualTo(OFFER_COUNTERSIGNED_EMAIL_BY_OTHER_PARTY);

        assertThat(referenceArgument.getAllValues().get(8))
            .matches("to-defendant-agreement-counter-signed-by-claimant-notification-"
                + CLAIM_REFERENCE_NUMBER
            );

        Map<String, String> emailData = new HashMap<>();
        emailData.put(COUNTER_SIGNING_PARTY_KEY, claim.getClaimData().getClaimant().getName());
        emailData.put(FRONTEND_BASE_URL_KEY, FRONTEND_BASE_URL);

        verifyEmailData(emailDataArgument.getAllValues().get(8), emailData);

        assertThat(senderArgument.getAllValues().get(8))
            .isEqualTo(claim.getClaimData().getDefendant().getEmail().orElse(null));
    }

    private ResultActions claimantRequestFor(String endpoint) throws Exception {
        return webClient
            .perform(
                post(format("/claims/%s/offers/%s/%s", claim.getExternalId(), MadeBy.CLAIMANT.name(), endpoint))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, CLAIMANT_AUTH_TOKEN)
            );
    }

    private ResultActions defendantRequestFor(String endpoint) throws Exception {
        return webClient
            .perform(
                post(format("/claims/%s/offers/%s/%s", claim.getExternalId(), MadeBy.DEFENDANT.name(), endpoint))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, DEFENDANT_AUTH_TOKEN)
            );
    }
}
