package uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegalOrderCoverSheetContentProviderTest {
    private static final String STAFF_NOTIFICATIONS_RECIPIENT = "email@domain.gov";

    private final Claim claim = SampleClaim.getDefault();

    @Mock
    private StaffEmailProperties staffEmailProperties;

    private LegalOrderCoverSheetContentProvider provider;

    @Before
    public void beforeEachTest() {
        provider = new LegalOrderCoverSheetContentProvider(
            staffEmailProperties
        );
        when(staffEmailProperties.getRecipient()).thenReturn(STAFF_NOTIFICATIONS_RECIPIENT);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaimForClaimant() {
        //noinspection ConstantConditions
        provider.createContentForClaimant(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaimForDefendant() {
        //noinspection ConstantConditions
        provider.createContentForDefendant(null);
    }

    @Test
    public void shouldProvideClaimantData() {
        Map<String, Object> content = provider.createContentForClaimant(claim);
        assertThat(content).containsEntry("partyFullName", "John Rambo");
        assertThat(content).containsEntry("partyAddress", claim.getClaimData().getClaimant().getAddress());
        assertThat(content).containsEntry("claimReferenceNumber", claim.getReferenceNumber());
        assertThat(content).containsEntry("hmctsEmail", STAFF_NOTIFICATIONS_RECIPIENT);
    }

    @Test
    public void shouldProvideClaimantDataWithTradingAsWhenSoleTrader() {
        SoleTrader claimant = SampleParty.builder().withRepresentative(null).soleTrader();
        Claim claim = SampleClaim.builder().withClaimData(
            SampleClaimData.builder().withClaimant(claimant).build()).build();

        Map<String, Object> content = provider.createContentForClaimant(claim);

        String expectedName = String.format("%s T/A %s",
            claimant.getName(),
            claimant.getBusinessName()
                .orElseThrow(() -> new IllegalStateException("Missing business name")));

        assertThat(content).containsEntry("partyFullName", expectedName);
        assertThat(content).containsEntry("partyAddress", claim.getClaimData().getClaimant().getAddress());
        assertThat(content).containsEntry("claimReferenceNumber", claim.getReferenceNumber());
        assertThat(content).containsEntry("hmctsEmail", STAFF_NOTIFICATIONS_RECIPIENT);
    }

    @Test
    public void shouldProvideDefendantData() {
        Map<String, Object> content = provider.createContentForDefendant(claim);

        assertThat(content).containsEntry("partyFullName", "Dr. John Smith");
        assertThat(content).containsEntry("partyAddress", claim.getClaimData().getClaimant().getAddress());
        assertThat(content).containsEntry("claimReferenceNumber", claim.getReferenceNumber());
        assertThat(content).containsEntry("hmctsEmail", STAFF_NOTIFICATIONS_RECIPIENT);
    }
}
