package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.PersonContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimContentProviderTest {
    private final Claim claimWithDefendantAddressNull = SampleClaim.getDefaultWithDefendantAddressNull();
    private final Claim claimDefault = SampleClaim.getDefault();

    private ClaimContentProvider provider = new ClaimContentProvider(
        new ClaimantContentProvider(
            new PersonContentProvider()
        ),
        new PersonContentProvider(),
        new ClaimDataContentProvider(
            new InterestContentProvider(
                new InterestCalculationService(Clock.systemDefaultZone())
        ))
    );

    @Test
    public void shouldReturnAddressByClaimantWhenAddressIsNull() {
        //when
        Map<String, Object> content = provider.createContent(claimWithDefendantAddressNull);
        Address address = SampleAddress.builder().build();

        assertThat(((PersonContent) content.get("defendant")).getAddress())
            .isEqualTo(address);
    }

    @Test
    public void shouldReturnAddressByDefendantWhenAddressIsNotNull() {
        //when
        Map<String, Object> content = provider.createContent(claimDefault);
        Address address = SampleAddress.builder().build();

        assertThat(((PersonContent) content.get("defendant")).getAddress())
            .isEqualTo(address);
    }
}
