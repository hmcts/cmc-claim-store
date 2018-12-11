package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmount;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmountBreakDown;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmountRange;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;

import java.math.BigDecimal;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.BREAK_DOWN;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.NOT_KNOWN;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.RANGE;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AmountMapperTest {

    @Autowired
    private AmountMapper amountMapper;

    @Test
    public void shouldMapAmountRangeToCCD() {
        //given
        Amount amount = SampleAmountRange.builder().build();

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

    @Test
    public void shouldMapAmountBreakDownToCCD() {
        //given
        Amount amount = SampleAmountBreakdown.builder().build();

        //when
        CCDAmount ccdAmount = amountMapper.to(amount);

        //then
        assertThat(amount).isEqualTo(ccdAmount);
    }

    @Test
    public void shouldMapAmountBreakDownFromCCD() {
        //given
        CCDAmount ccdAmount = CCDAmount.builder()
            .type(BREAK_DOWN)
            .amountBreakDown(
                CCDAmountBreakDown.builder()
                    .rows(singletonList(
                        CCDCollectionElement.<CCDAmountRow>builder()
                            .value(CCDAmountRow.builder()
                                .amount(BigDecimal.valueOf(50))
                                .reason("payment")
                                .build()
                            )
                            .build()
                    )).build()
            )
            .build();

        //when
        Amount amount = amountMapper.from(ccdAmount);

        //then
        assertThat(amount).isEqualTo(ccdAmount);
    }

    @Test
    public void shouldMapAmountNotKnownToCCD() {
        //given
        Amount amount = new NotKnown();

        //when
        CCDAmount ccdAmount = amountMapper.to(amount);

        //then
        assertThat(amount).isEqualTo(ccdAmount);
    }

    @Test
    public void shouldMapAmountNotKnownFromCCD() {
        //given
        CCDAmount ccdAmount = CCDAmount.builder().type(NOT_KNOWN).build();

        //when
        Amount amount = amountMapper.from(ccdAmount);

        //then
        assertThat(amount).isEqualTo(ccdAmount);
    }
}
