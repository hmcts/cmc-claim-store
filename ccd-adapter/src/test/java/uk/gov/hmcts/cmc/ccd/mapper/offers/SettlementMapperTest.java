package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SamplePartyStatement;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SettlementMapperTest {

    @Autowired
    private SettlementMapper settlementMapper;

    @Test
    public void toCCDPartyStatementsReturnNullWhenSettlementIsNull() {
        assertNull(
            settlementMapper.toCCDPartyStatements(null)
        );
    }

    @Test
    public void toCCDPartyStatementsWithEmptySettlementReturnsNull() {
        //Given
        Settlement emptySettlement = new Settlement();

        //When
        List<CCDCollectionElement<CCDPartyStatement>> partystatements =
            settlementMapper.toCCDPartyStatements(emptySettlement);

        //Then
        assertNull(partystatements);
    }

    @Test
    public void toCCDPartyStatementMapsAllPartyStatement() {
        //Given
        Settlement settlement = new SampleSettlement().withPartyStatements(
            SamplePartyStatement.offerPartyStatement,
            SamplePartyStatement.acceptPartyStatement,
            SamplePartyStatement.counterSignPartyStatement
        ).build();

        //When
        List<CCDCollectionElement<CCDPartyStatement>> partyStatements =
            settlementMapper.toCCDPartyStatements(settlement);

        //Then
        assertNotNull(partyStatements);
        assertThat(partyStatements.size(), is(3));
        assertThat(partyStatements.get(0), isA(CCDCollectionElement.class));

        partyStatements.forEach(partyStatementListElement -> {
            assertNotNull(partyStatementListElement.getValue());
            assertThat(partyStatementListElement.getValue(), isA(CCDPartyStatement.class));
        });
    }

    @Test
    public void fromCCDDefendantReturnNullWhenDefendantIsNull() {
        assertNull(
            settlementMapper.fromCCDDefendant(CCDRespondent.builder().build())
        );
    }

    @Test
    public void toCCDPartyStatementsWithDefendantHasNoSettlementReturnsNull() {
        //Given
        CCDRespondent ccdRespondent = CCDRespondent.builder().build();

        //When
        Settlement settlement =
            settlementMapper.fromCCDDefendant(ccdRespondent);

        //Then
        assertNull(settlement);
    }

    @Test
    public void toCCDPartyStatementsMapsSettlementFromCCDPartyStatement() {
        //Given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withPartyStatements().build();

        //When
        Settlement settlement =
            settlementMapper.fromCCDDefendant(ccdRespondent);

        //Then
        assertNotNull(settlement);
        assertNotNull(settlement.getPartyStatements());
        assertThat(settlement.getPartyStatements().size(), is(3));
        assertThat(settlement.getPartyStatements().get(0), isA(PartyStatement.class));

        assertTrue(settlement.getPartyStatements().stream().anyMatch(partyStatement ->
                partyStatement.getMadeBy() == MadeBy.DEFENDANT
                    && partyStatement.getType() == StatementType.OFFER
            )
        );

        assertTrue(settlement.getPartyStatements().stream().anyMatch(partyStatement ->
                partyStatement.getMadeBy() == MadeBy.CLAIMANT
                    && partyStatement.getType() == StatementType.ACCEPTATION
            )
        );

        assertTrue(settlement.getPartyStatements().stream().anyMatch(partyStatement ->
                partyStatement.getMadeBy() == MadeBy.DEFENDANT
                    && partyStatement.getType() == StatementType.COUNTERSIGNATURE
            )
        );
    }
}
