package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDPartyStatementArrayElement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDSettlement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SettlementMapperTest {

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private PartyStatementMapper partyStatementMapper;

    @Test
    public void shouldMapSettlementToCCD() {
        //given
        final Settlement settlement = SampleSettlement.validDefaults();

        //when
        CCDSettlement ccdSettlement = settlementMapper.to(settlement);

        //then
        assertThat(settlement).isEqualTo(ccdSettlement);
    }

    @Test
    public void shouldMapRejectionSettlementToCCD() {
        //given
        final Settlement settlement = SampleSettlement.builder()
            .withPartyStatements(SampleSettlement.offerPartyStatement, SampleSettlement.rejectPartyStatement)
            .build();

        //when
        CCDSettlement ccdSettlement = settlementMapper.to(settlement);

        //then
        assertThat(settlement).isEqualTo(ccdSettlement);
    }

    @Test
    public void shouldMapAcceptanceSettlementToCCD() {
        //given
        final Settlement settlement = SampleSettlement.builder()
            .withPartyStatements(SampleSettlement.offerPartyStatement, SampleSettlement.acceptPartyStatement).build();

        //when
        CCDSettlement ccdSettlement = settlementMapper.to(settlement);

        //then
        assertThat(settlement).isEqualTo(ccdSettlement);
    }


    @Test
    public void shouldMaintainTheOrderOfPartyStatements() {
        //given
        PartyStatement[] partyStatements = {
            SampleSettlement.offerPartyStatement,
            SampleSettlement.rejectPartyStatement,
            SampleSettlement.offerPartyStatement,
            SampleSettlement.rejectPartyStatement,
            SampleSettlement.offerPartyStatement,
            SampleSettlement.acceptPartyStatement
        };

        final Settlement settlement = SampleSettlement.builder()
            .withPartyStatements(partyStatements).build();

        //when
        CCDSettlement ccdSettlement = settlementMapper.to(settlement);

        //then
        assertThat(settlement).isEqualTo(ccdSettlement);

        assertThat(partyStatements)
            .isEqualTo(
                ccdSettlement.getPartyStatements().stream()
                    .map(CCDPartyStatementArrayElement::getValue)
                    .map(partyStatementMapper::from)
                    .toArray()
            );
    }
}
