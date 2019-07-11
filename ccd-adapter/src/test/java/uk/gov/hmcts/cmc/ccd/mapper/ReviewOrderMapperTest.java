package uk.gov.hmcts.cmc.ccd.mapper;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDReviewOrder;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleReviewOrder;

import java.time.LocalDateTime;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDReviewOrder.RequestedBy.DEFENDANT;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
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
        Assertions.assertThat(ccdReviewOrder).isNull();
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
        Assertions.assertThat(reviewOrder).isNull();
    }

}
