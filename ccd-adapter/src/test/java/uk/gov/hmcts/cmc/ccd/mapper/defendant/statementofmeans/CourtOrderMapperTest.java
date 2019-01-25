package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CourtOrderMapperTest {

    @Autowired
    private CourtOrderMapper mapper;

    @Test
    public void shouldMapCourtOrderToCCD() {
        //given
        CourtOrder courtOrder = CourtOrder.builder()
            .amountOwed(BigDecimal.TEN)
            .claimNumber("Reference")
            .monthlyInstalmentAmount(BigDecimal.ONE)
            .build();

        //when
        CCDCollectionElement<CCDCourtOrder> ccdCourtOrder = mapper.to(courtOrder);

        //then
        assertThat(courtOrder).isEqualTo(ccdCourtOrder.getValue());
        assertThat(courtOrder.getId()).isEqualTo(ccdCourtOrder.getId());
    }

    @Test
    public void shouldMapCourtOrderFromCCD() {
        //given
        CCDCourtOrder ccdCourtOrder = CCDCourtOrder.builder()
            .amountOwed(BigDecimal.TEN)
            .claimNumber("Reference")
            .monthlyInstalmentAmount(BigDecimal.ONE)
            .build();

        //when
        CourtOrder courtOrder = mapper.from(CCDCollectionElement.<CCDCourtOrder>builder().value(ccdCourtOrder).build());

        //then
        assertThat(courtOrder).isEqualTo(ccdCourtOrder);
    }
}
