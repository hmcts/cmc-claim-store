package uk.gov.hmcts.cmc.domain.utils;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;

public class FeaturesUtilsTest {

    @Test
    public void shouldReturnFalseWhereClaimFeaturesAreNull() {
        assertFalse(FeaturesUtils.isOnlineDQ(SampleClaim.builder().withFeatures(null).build()));
    }

    @Test
    public void shouldReturnFalseWhereClaimFeaturesAreEmpty() {
        assertFalse(FeaturesUtils
            .isOnlineDQ(SampleClaim.builder().withFeatures(Collections.emptyList()).build()));
    }

    @Test
    public void shouldReturnFalseWhereClaimFeaturesDoesNotHasDirectionQuestionnaire() {
        assertFalse(FeaturesUtils
            .isOnlineDQ(SampleClaim.builder().withFeatures(ImmutableList.of(ADMISSIONS.getValue())).build())
        );
    }

    @Test
    public void shouldReturnTrueWhereClaimFeaturesHasDirectionQuestionnaire() {
        assertTrue(FeaturesUtils
            .isOnlineDQ(SampleClaim.builder()
                .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
                .build())
        );
    }
}
