package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDReviewOrder;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleReviewOrder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDReviewOrder.RequestedBy.DEFENDANT;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@ExtendWith(SpringExtension.class)
public class ReviewOrderMapperTest {

    @Autowired
    ReviewOrderMapper mapper;

    @Test
    public void shouldMapReviewOrderToCCD() {
        ReviewOrder reviewOrder = SampleReviewOrder.getDefault();

        CCDReviewOrder ccdReviewOrder = mapper.to(reviewOrder);

        assertThat(reviewOrder).isEqualTo(ccdReviewOrder);
    }

    @Test
    public void shouldMapNullReviewOrderToCCD() {
        CCDReviewOrder ccdReviewOrder = mapper.to(null);
        assertNull(ccdReviewOrder);
    }

    @Test
    public void shouldMapReviewOrderFromCCD() {
        CCDReviewOrder ccdReviewOrder = CCDReviewOrder.builder()
            .reason("My reason")
            .requestedBy(DEFENDANT)
            .requestedAt(LocalDateTime.now())
            .build();

        ReviewOrder reviewOrder = mapper.from(ccdReviewOrder);

        assertThat(reviewOrder).isEqualTo(ccdReviewOrder);
    }

    @Test
    public void shouldMapNullCCDReviewOrderFromCCD() {
        ReviewOrder reviewOrder = mapper.from(null);
        assertNull(reviewOrder);
    }
}
