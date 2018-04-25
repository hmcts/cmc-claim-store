package uk.gov.hmcts.cmc.rpa.mapper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.config.RpaAdapterConfig;
import uk.gov.hmcts.cmc.rpa.domain.Case;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = RpaAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CaseMapperTest {

    @Autowired
    private CaseMapper rpaCaseMapper;

    @Test
    public void shouldMapCitizenClaimToRPA() throws JsonProcessingException {
        //given
        Claim claim = SampleClaim.getDefault();

        //when
        Case rpaCase = rpaCaseMapper.to(claim);

        String result = new ObjectMapper().writeValueAsString(rpaCase).trim();

        String expected = new ResourceReader().read("/rpa_case.json").trim();

        //then
        assertThat(result).isEqualTo(expected);
    }

}
