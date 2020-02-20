package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.HearingContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.PartyDetailsContent;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_RESPONSE;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class DefendantResponseContentProviderTest {

    private final Claim claim = SampleClaim.getWithDefaultResponse();
    private final HearingContentProvider hearingContentProvider =
        new HearingContentProvider();

    private final DefendantResponseContentProvider provider = new DefendantResponseContentProvider(
        new PartyDetailsContentProvider(),
        new ClaimDataContentProvider(
            new InterestContentProvider(
                new InterestCalculationService(Clock.systemDefaultZone())
            )
        ),
        new NotificationsProperties(),
        new FullDefenceResponseContentProvider(hearingContentProvider),
        new FullAdmissionResponseContentProvider(
            new PaymentIntentionContentProvider(),
            new StatementOfMeansContentProvider()
        ),
        new PartAdmissionResponseContentProvider(
            new PaymentIntentionContentProvider(),
            new StatementOfMeansContentProvider(),
            hearingContentProvider
        )
    );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        provider.createContent(null);
    }

    @Test
    public void shouldProvideClaimReferenceNumber() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(((ClaimContent) content.get("claim")).getReferenceNumber())
            .isEqualTo(claim.getReferenceNumber());

    }

    @Test
    public void shouldProvideClaimSubmittedOn() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(((ClaimContent) content.get("claim")).getIssuedOn())
            .isEqualTo(formatDate(claim.getIssuedOn()));
    }

    @Test
    public void shouldProvideClaimantFullName() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(((ClaimContent) content.get("claim")).getStatementOfTruth().getSignerName())
            .isEqualTo(claim.getClaimData().getClaimant().getName());
    }

    @Test
    public void shouldProvideDefendantDetails() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).containsKey("defendant");
        assertThat(content.get("defendant")).isInstanceOf(PartyDetailsContent.class);
    }

    @Test
    public void shouldProvideResponseDefence() {
        Map<String, Object> content = provider.createContent(claim);

        List<String> expected = ((FullDefenceResponse) claim.getResponse()
            .orElseThrow(() -> new AssertionError(MISSING_RESPONSE)))
            .getDefence()
            .map(ImmutableList::of)
            .map(immutableList -> (List<String>) immutableList)
            .orElseGet(Collections::emptyList);
        assertThat(content)
            .containsEntry("responseDefence", expected);
    }

    @Test
    public void shouldProvideCorrectFormNumber() {
        Map<String, Object> content = provider.createContent(claim);
        assertThat(content)
            .containsEntry("formNumber", "OCON9B");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProvidePaymentDeclaration() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).containsKey("paymentDeclaration");
        assertThat(content.get("paymentDeclaration")).isInstanceOf(Map.class);
        assertThat((Map<String, String>) content.get("paymentDeclaration"))
            .containsOnlyKeys("paidDate", "explanation")
            .containsEntry("paidDate", "2 January 2016")
            .containsEntry("explanation", "Paid cash");
    }

    @Test
    public void shouldProvideResponseTimeline() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content)
            .containsKeys("timelineComment", "events");
    }

    @Test
    public void shouldProvideAmountPaid() {
        Claim statesPaidClaim = SampleClaim.getClaimWithFullDefenceAlreadyPaid();
        Map<String, Object> content = provider.createContent(statesPaidClaim);

        assertThat(content)
            .containsEntry("paidAmount", "£100.99")
            .containsEntry("hasDefendantAlreadyPaid", true);
    }

    @Test
    public void shouldProvideMediation() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content)
            .containsEntry("mediation", true);
    }
}
