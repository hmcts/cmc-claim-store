package uk.gov.hmcts.cmc.ccd.deprecated.mapper.claimantresponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.SampleData;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDFormaliseOption.SETTLEMENT;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantAcceptanceMapperTest {

    @Autowired
    private ResponseAcceptationMapper mapper;

    @Test
    public void shouldMapResponseAcceptanceWithCCJFormalisationFromCCD() {
        //given
        CCDResponseAcceptation ccdResponseAcceptation = SampleData.getResponseAcceptation(CCDFormaliseOption.CCJ);

        //when
        ResponseAcceptation response = mapper.from(ccdResponseAcceptation);

        //then
        assertThat(response).isEqualTo(ccdResponseAcceptation);
    }

    @Test
    public void shouldMapResponseAcceptanceWithSettlementFormalisationFromCCD() {
        //given
        CCDResponseAcceptation ccdResponseAcceptation = SampleData.getResponseAcceptation(SETTLEMENT);

        //when
        ResponseAcceptation response = mapper.from(ccdResponseAcceptation);

        //then
        assertThat(response).isEqualTo(ccdResponseAcceptation);
    }
}
