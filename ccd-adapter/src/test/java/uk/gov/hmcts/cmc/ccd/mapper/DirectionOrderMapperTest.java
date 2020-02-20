package uk.gov.hmcts.cmc.ccd.mapper;

import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionOrder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.addCCDOrderGenerationData;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DirectionOrderMapperTest {

    @Autowired
    DirectionOrderMapper mapper;

    @Test
    public void shouldMapDirectionOrderFromCCD() {
        CCDDirectionOrder ccdDirectionOrder = CCDDirectionOrder.builder()
            .hearingCourtAddress(SampleData.getHearingCourtAddress())
            .hearingCourtName(SampleData.MANCHESTER_CIVIL_JUSTICE_CENTRE_CIVIL_AND_FAMILY_COURTS)
            .createdOn(LocalDateTime.now())
            .build();

        Claim.ClaimBuilder claimBuilder = Claim.builder();
        CCDCase ccdCase = addCCDOrderGenerationData(CCDCase.builder().directionOrder(ccdDirectionOrder).build());
        mapper.from(ccdCase, claimBuilder);

        DirectionOrder directionOrder = claimBuilder.build().getDirectionOrder()
            .orElseThrow(() -> new AssertionFailedError("Missing direction order"));
        assertThat(directionOrder).isEqualTo(ccdDirectionOrder);
        assertThat(directionOrder.getDirections()).hasSize(4);

        assertEnumNames(directionOrder.getGrantExpertReportPermission(),
            ccdCase.getGrantExpertReportPermission());

        assertEnumNames(directionOrder.getExpertReportPermissionAskedByClaimant(),
            ccdCase.getExpertReportPermissionPartyAskedByClaimant());

        assertEnumNames(directionOrder.getExpertReportPermissionAskedByDefendant(),
            ccdCase.getExpertReportPermissionPartyAskedByDefendant());

    }

    private void assertEnumNames(YesNoOption input, CCDYesNoOption expected) {
        assertThat(input.name()).isEqualTo(expected.name());
    }

    @Test
    public void shouldMapNullCCDDirectionOrderFromCCD() {
        Claim.ClaimBuilder claimBuilder = Claim.builder();
        CCDCase ccdCase = CCDCase.builder().build();
        mapper.from(ccdCase, claimBuilder);
        assertThat(!claimBuilder.build().getDirectionOrder().isPresent());
    }

    @Test
    public void shouldMapNullCCDOrderGenerationDataFromCCD() {
        CCDDirectionOrder ccdDirectionOrder = CCDDirectionOrder.builder()
            .hearingCourtAddress(SampleData.getHearingCourtAddress())
            .hearingCourtName(SampleData.MANCHESTER_CIVIL_JUSTICE_CENTRE_CIVIL_AND_FAMILY_COURTS)
            .createdOn(LocalDateTime.now())
            .build();

        Claim.ClaimBuilder claimBuilder = Claim.builder();
        CCDCase ccdCase = CCDCase.builder().directionOrder(ccdDirectionOrder).build();
        mapper.from(ccdCase, claimBuilder);
        assertThat(claimBuilder.build().getDirectionOrder().isPresent());
    }
}
