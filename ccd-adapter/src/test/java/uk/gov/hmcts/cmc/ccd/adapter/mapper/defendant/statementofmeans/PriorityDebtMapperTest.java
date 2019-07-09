package uk.gov.hmcts.cmc.ccd.adapter.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPaymentFrequency;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPriorityDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.adapter.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPriorityDebtType.COUNCIL_TAX_COMMUNITY_CHARGE;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency.MONTH;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt.PriorityDebtType.ELECTRICITY;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PriorityDebtMapperTest {

    @Autowired
    private PriorityDebtMapper mapper;

    @Test
    public void shouldMapPriorityDebtToCCD() {
        //given
        PriorityDebt priorityDebt = PriorityDebt.builder()
            .type(ELECTRICITY)
            .frequency(MONTH)
            .amount(BigDecimal.TEN)
            .build();

        //when
        CCDCollectionElement<CCDPriorityDebt> ccdPriorityDebt = mapper.to(priorityDebt);

        //then
        assertThat(priorityDebt).isEqualTo(ccdPriorityDebt.getValue());
        assertThat(priorityDebt.getId()).isEqualTo(ccdPriorityDebt.getId());
    }

    @Test
    public void shouldMapPriorityDebtFromCCD() {
        //given
        CCDPriorityDebt ccdPriorityDebt = CCDPriorityDebt.builder()
            .type(COUNCIL_TAX_COMMUNITY_CHARGE)
            .frequency(CCDPaymentFrequency.MONTH)
            .amount("1000")
            .build();

        String collectionId = UUID.randomUUID().toString();

        //when
        PriorityDebt priorityDebt = mapper.from(CCDCollectionElement.<CCDPriorityDebt>builder()
            .id(collectionId)
            .value(ccdPriorityDebt).build());

        //then
        assertThat(priorityDebt).isEqualTo(ccdPriorityDebt);
        assertThat(priorityDebt.getId()).isEqualTo(collectionId);
    }
}
