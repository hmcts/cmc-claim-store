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
public class MoreTimeRequestedJsonMapperTest {

    private static final String ADDITIONAL_TIME = "/more-time-requested/rpa_more_time_requested.json";
    private static final String ADDTIONAL_TIME_ADDRESS_MODIFIED = "/more-time-requested/rpa_more_time_requested_address_modified.json";
    private static final String REFERENCE_NUMBER = "000MC001";

    @Autowired
    private MoreTimeRequestedJsonMapper mapper;

    @Test
    public void shouldMapPartyForMoreTimeRequested() throws JSONException {

        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().individual())
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withReferenceNumber(REFERENCE_NUMBER)
            .withIssuedOn(LocalDate.of(2018, 4, 10))
            .withResponseDeadline(LocalDate.of(2018, 5, 27))
            .build();

        String expected = new ResourceReader().read(ADDITIONAL_TIME).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapPartyForMoreTimeRequestedAddressModified() throws JSONException {

        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().individual())
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withReferenceNumber(REFERENCE_NUMBER)
            .withIssuedOn(LocalDate.of(2018, 4, 10))
            .withResponseDeadline(LocalDate.of(2018, 5, 27))
            .build();

        String expected = new ResourceReader().read(ADDTIONAL_TIME_ADDRESS_MODIFIED).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }
}
