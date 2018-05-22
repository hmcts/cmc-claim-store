package uk.gov.hmcts.cmc.rpa.mapper;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.config.ModuleConfiguration;

import java.time.LocalDate;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DefenceResponseJsonMapperTest {

    @Autowired
    private DefenceResponseJsonMapper mapper;
    private static String INPUT = "/defence_response_rpa_case.json";

    @Test
    public void shouldMapIndividualDefenceResponseToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read(INPUT).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }
}
