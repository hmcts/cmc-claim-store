package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

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

    private final Claim claim = SampleClaim.getDefault();

    @Mock
    private NotificationsProperties notificationsProperties;

    private DefendantPinLetterContentProvider provider;

    @Before
    public void beforeEachTest() {
        provider = new DefendantPinLetterContentProvider(notificationsProperties,
            new InterestContentProvider(
                new InterestCalculationService(Clock.systemDefaultZone())
            )
        );
        when(notificationsProperties.getRespondToClaimUrl()).thenReturn(RESPOND_TO_CLAIM_URL);
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
    public void shouldProvideDefendantName() {
        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        assertThat(content).containsEntry("defendantFullName", "John Smith");
    }

    @Test
    public void shouldProvideClaimAmount() {
        Map<String, Object> content = provider.createContent(claim, DEFENDANT_PIN);

        assertThat(content).containsEntry("claimTotalAmount", "£80.89");
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

}
