package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.orders.BespokeOrderDirection;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.addCCDBespokeOrderGenerationData;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@ExtendWith(SpringExtension.class)
public class BespokeOrderDirectionMapperTest {

    @Autowired
    BespokeOrderDirectionMapper mapper;

    @Test
    public void shouldMapBespokeDirectionOrderFromCCD() {
        Claim.ClaimBuilder claimBuilder = Claim.builder();
        CCDCase ccdCase = addCCDBespokeOrderGenerationData(CCDCase.builder().build());
        mapper.from(ccdCase, claimBuilder);

        BespokeOrderDirection bespokeOrderDirection = claimBuilder.build().getBespokeOrderDirection()
            .orElseThrow(() -> new AssertionFailedError("Missing bespoke direction order"));
        assertThat(bespokeOrderDirection.getBespokeDirections()).hasSize(1);
    }

    @Test
    public void shouldMapNullCCDDirectionOrderFromCCD() {
        Claim.ClaimBuilder claimBuilder = Claim.builder();
        CCDCase ccdCase = CCDCase.builder().build();
        mapper.from(ccdCase, claimBuilder);
        assertThat(claimBuilder.build().getBespokeOrderDirection()).isEmpty();
    }
}
