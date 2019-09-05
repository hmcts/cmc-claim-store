package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.sampledata.SampleCCDCaseData;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionOrder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.sampledata.SampleCCDCaseData.getCCDOrderGenerationData;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DirectionOrderMapperTest {

    @Autowired
    DirectionOrderMapper mapper;

    @Test
    public void shouldMapDirectionOrderFromCCD() {
        CCDDirectionOrder ccdDirectionOrder = CCDDirectionOrder.builder()
            .hearingCourtAddress(SampleCCDCaseData.getCCDAddress())
            .createdOn(LocalDateTime.now())
            .build();

        DirectionOrder directionOrder = mapper.from(ccdDirectionOrder, getCCDOrderGenerationData());

        assertThat(directionOrder).isEqualTo(ccdDirectionOrder);
        assertThat(directionOrder.getDirections()).hasSize(4);
    }

    @Test
    public void shouldMapNullCCDDirectionOrderFromCCD() {
        DirectionOrder directionOrder = mapper.from(null, getCCDOrderGenerationData());
        assertNull(directionOrder);
    }

    @Test
    public void shouldMapNullCCDOrderGenerationDataFromCCD() {
        CCDDirectionOrder ccdDirectionOrder = CCDDirectionOrder.builder()
            .hearingCourtAddress(SampleCCDCaseData.getCCDAddress())
            .createdOn(LocalDateTime.now())
            .build();

        DirectionOrder directionOrder = mapper.from(ccdDirectionOrder, null);
        assertNull(directionOrder);
    }
}
