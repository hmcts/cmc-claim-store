package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Assert;
import org.junit.Test;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CaseDataExtractorUtilsTest {

    @Test
    public void defendantForBulkPrintShouldHaveBP() {
        Claim claim = mock(Claim.class);
        when(claim.getClaimData()).thenReturn(SampleClaimData.builder().withDefendant(SampleTheirDetails.builder().withName("Dr. John Smith").partyDetails()).build());
        List<String> list = CaseDataExtractorUtils.getDefendantForBulkPrint(claim);
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0), "Dr. John Smith (BP)");
    }

    @Test
    public void whenDefendantForBulkPrintHaveMoreThan1Defendant() {
        Claim claim = mock(Claim.class);
        List<TheirDetails> defendants =
            List.of(SampleTheirDetails.builder().withName("Dr. John Smith").partyDetails(), SampleTheirDetails.builder().withName("Dr. Jolly Smith").partyDetails());
        when(claim.getClaimData()).thenReturn(SampleClaimData.builder().withDefendants(defendants).build());
        Assert.assertThrows(IllegalStateException.class, () -> CaseDataExtractorUtils.getDefendantForBulkPrint(claim));
    }
}
