package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SamplePartyStatement;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;



@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PartyStatementMapperTest {

    @Autowired
    private PartyStatementMapper partyStatementMapper;

    @Test
    public void shouldMapCountyCourtJudgmentToCCD() {
        //given
        final PartyStatement partyStatement = SamplePartyStatement.builder()
            .build();

        //when
        CCDPartyStatement ccdPartyStatement = partyStatementMapper.to(partyStatement);

        //then
        assertThat(partyStatement).isEqualTo(ccdPartyStatement);
    }

}
