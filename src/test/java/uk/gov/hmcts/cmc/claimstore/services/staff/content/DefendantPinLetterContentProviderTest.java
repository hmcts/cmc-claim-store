package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.RESPONSE_DEADLINE;

@RunWith(MockitoJUnitRunner.class)
public class DefendantPinLetterContentProviderTest {

    private static final String DEFENDANT_PIN = "dsf4dd2";
    private static final String RESPOND_TO_CLAIM_URL = "https://moneyclaim.hmcts.net/first-contact/start";
    private static final String STAFF_NOTIFICATIONS_RECIPIENT = "email@domain.gov";

    private final Claim claim = SampleClaim.getDefault();

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private StaffEmailProperties staffEmailProperties;

    private DefendantPinLetterContentProvider provider;

    @Before
    public void beforeEachTest() {
        provider = new DefendantPinLetterContentProvider(
            notificationsProperties,
            staffEmailProperties,
            new InterestContentProvider(
                new InterestCalculationService(Clock.systemDefaultZone())
            )
        );
        when(notificationsProperties.getRespondToClaimUrl()).thenReturn(RESPOND_TO_CLAIM_URL);
        when(staffEmailProperties.getRecipient()).thenReturn(STAFF_NOTIFICATIONS_RECIPIENT);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        provider.createContent(null, DEFENDANT_PIN);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullDefendantPin() {
        provider.createContent(claim, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenGivenEmptyDefendantPin() {
        provider.createContent(claim, "");
    }

    @Test
    public void shouldProvideClaimantName() {
        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        assertThat(content).containsEntry("claimantFullName", "John Rambo");
    }

    @Test
    public void shouldProvideClaimantNameWithTradingAsWhenSoleTrader() {
        SoleTrader claimant = SampleParty.builder().withRepresentative(null).soleTrader();
        Claim claim = SampleClaim.builder().withClaimData(
            SampleClaimData.builder().withClaimant(claimant).build()).build();

        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        String expected = String.format("%s T/A %s",
            claimant.getName(),
            claimant.getBusinessName()
                .orElseThrow(() -> new IllegalStateException("Missing business name")));

        assertThat(content).containsEntry("claimantFullName", expected);

    }

    @Test
    public void shouldProvideDefendantName() {
        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        assertThat(content).containsEntry("defendantFullName", "Dr. John Smith");
    }

    @Test
    public void shouldProvideClaimAmount() {
        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        assertThat(content).containsEntry("claimTotalAmount", "£81.91");
    }

    @Test
    public void shouldProvideRespondToClaimUrl() {
        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        assertThat(content).containsEntry("respondToClaimUrl", RESPOND_TO_CLAIM_URL);
    }

    @Test
    public void shouldProvideClaimReferenceNumber() {
        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        assertThat(content).containsEntry("claimReferenceNumber", "000CM001");
    }

    @Test
    public void shouldProvideDefendantPin() {
        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        assertThat(content).containsEntry("defendantPin", DEFENDANT_PIN);
    }

    @Test
    public void shouldProvideResponseDeadline() {
        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        assertThat(content).containsEntry("responseDeadline", formatDate(RESPONSE_DEADLINE));
    }

    @Test
    public void shouldProvideHmctsEmail() {
        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        assertThat(content).containsEntry("hmctsEmail", STAFF_NOTIFICATIONS_RECIPIENT);
    }

}
