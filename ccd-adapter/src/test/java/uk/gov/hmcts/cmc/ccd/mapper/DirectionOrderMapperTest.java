package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.domain.models.DirectionOrder;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionOrder;

import java.time.LocalDateTime;

import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DirectionOrderMapperTest {

    @Autowired
    DirectionOrderMapper mapper;

    @Test
    public void shouldMapDirectionOrderToCCD() {
        DirectionOrder directionOrder = SampleDirectionOrder.getDefault();

        CCDDirectionOrder ccdDirectionOrder = mapper.to(directionOrder);

        assertThat(directionOrder).isEqualTo(ccdDirectionOrder);
    }

    @Test
    public void shouldMapNullDirectionOrderToCCD() {
        CCDDirectionOrder ccdReviewOrder = mapper.to(null);
        assertNull(ccdReviewOrder);
    }

    @Test
    public void shouldMapDirectionOrderFromCCD() {
        CCDDirectionOrder ccdDirectionOrder = CCDDirectionOrder.builder()
            .hearingCourtAddress(SampleData.getCCDAddress())
            .createdOn(LocalDateTime.now())
            .build();

        DirectionOrder directionOrder = mapper.from(ccdDirectionOrder);

        assertThat(directionOrder).isEqualTo(ccdDirectionOrder);
    }

    @Test
    public void shouldMapNullCCDDirectionOrderFromCCD() {
        DirectionOrder directionOrder = mapper.from(null);
        assertNull(directionOrder);
    }
}
