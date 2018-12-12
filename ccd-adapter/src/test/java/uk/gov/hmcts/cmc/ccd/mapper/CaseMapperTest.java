package uk.gov.hmcts.cmc.ccd.mapper;

import com.fasterxml.jackson.core.JsonFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.JsonMapper;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;


@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CaseMapperTest {

    @Autowired
    private CaseMapper ccdCaseMapper;

    @Autowired
    private JsonMapper ccdJsonMapper;

    @Test
    public void shouldMapLegalClaimToCCD() {
        //given
        Claim claim = SampleClaim.getDefaultForLegal();

        //when
        CCDCase ccdCase = ccdCaseMapper.to(claim);

        //then
        String json = ccdJsonMapper.toJson(ccdCase);
        System.out.println(json);

        CCDCase aCase = ccdJsonMapper.fromJson(json, CCDCase.class);

        Claim from = ccdCaseMapper.from(aCase);
        String output = ccdJsonMapper.toJson(from);
        System.out.println(output);
    }
}
