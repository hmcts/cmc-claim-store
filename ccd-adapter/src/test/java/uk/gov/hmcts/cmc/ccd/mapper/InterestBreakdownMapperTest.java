package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestBreakdown;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringRunner.class)
public class InterestBreakdownMapperTest {

    @Autowired
    private InterestBreakdownMapper mapper;

    @Test
    public void shouldCorrectlyMapToCCDObject() {
        InterestBreakdown interestBreakdown = SampleInterestBreakdown.validDefaults();

        CCDInterestBreakdown ccdObject = mapper.to(interestBreakdown);

        assertThat(interestBreakdown).isEqualTo(ccdObject);
    }

    @Test
    public void shouldMapNullInterestBreakdownToNull() {
        CCDInterestBreakdown ccdObject = mapper.to(null);
        assertThat(ccdObject).isNull();
    }

    @Test
    public void shouldCorrectlyMapFromCCDObject() {
        CCDInterestBreakdown ccdObject = CCDInterestBreakdown.builder()
            .totalAmount(BigDecimal.TEN)
            .explanation("I entered this amount because...")
            .build();

        InterestBreakdown interestBreakdown = mapper.from(ccdObject);

        assertThat(interestBreakdown).isEqualTo(ccdObject);
    }

    @Test
    public void shouldMapNullFromNullCCDObject() {
        InterestBreakdown interestBreakdown = mapper.from(null);
        assertThat(interestBreakdown).isNull();
    }

}
