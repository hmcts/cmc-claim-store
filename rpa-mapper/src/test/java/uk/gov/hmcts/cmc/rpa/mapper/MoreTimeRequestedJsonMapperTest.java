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
import java.time.LocalDateTime;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class MoreTimeRequestedJsonMapperTest {

    @Autowired
    private MoreTimeRequestedJsonMapper mapper;
    private static String REFERENCE_NUMBER = "000MC001";
    private static String INPUT = "/rpa_more_time_requested_forthwith.json";

    @Test
    public void shouldMapIndividualForMoreTimeRequested() throws JSONException {

        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().individual())
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withReferenceNumber(REFERENCE_NUMBER)
            .withIssuedOn(LocalDate.of(2018, 4, 10))
            .withResponseDeadline(LocalDate.of(2018, 5, 27))
            .build();

        String expected = new ResourceReader().read(INPUT).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapSoleTraderForMoreTimeRequested() throws JSONException {

        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().soleTrader())
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withReferenceNumber(REFERENCE_NUMBER)
            .withIssuedOn(LocalDate.of(2018, 4, 10))
            .withResponseDeadline(LocalDate.of(2018, 5, 27))
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        String expected = new ResourceReader().read(INPUT).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapOrganisationForMoreTimeRequested() throws JSONException {

        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().organisation())
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withReferenceNumber(REFERENCE_NUMBER)
            .withIssuedOn(LocalDate.of(2018, 4, 10))
            .withResponseDeadline(LocalDate.of(2018, 5, 27))
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        String expected = new ResourceReader().read(INPUT).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapCompanyForMoreTimeRequested() throws JSONException {

        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().company())
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withReferenceNumber(REFERENCE_NUMBER)
            .withIssuedOn(LocalDate.of(2018, 4, 10))
            .withResponseDeadline(LocalDate.of(2018, 5, 27))
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        String expected = new ResourceReader().read(INPUT).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

}
