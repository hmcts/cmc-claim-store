package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmount;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.RANGE;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AmountMapperTest {

    @Autowired
    private AmountMapper amountMapper;

    @Test
    public void shouldMapAmountRangeToCCD() {
        //given
        Amount amount = SampleAmountRange.validDefaults();

        //when
        CCDAmount ccdAmount = amountMapper.to(amount);

        //then
        assertThat(amount).isEqualTo(ccdAmount);
    }

    @Test
    public void shouldMapAmountRangeFromCCD() {
        //given
        CCDAmount ccdAmount = CCDAmount.builder().type(RANGE)
            .amountRange(CCDAmountRange.builder()
                .lowerValue(BigDecimal.valueOf(50))
                .higherValue(BigDecimal.valueOf(500))
                .build())
            .build();

        //when
        Amount amount = amountMapper.from(ccdAmount);

        //then
        assertThat(amount).isEqualTo(ccdAmount);
    }
}
