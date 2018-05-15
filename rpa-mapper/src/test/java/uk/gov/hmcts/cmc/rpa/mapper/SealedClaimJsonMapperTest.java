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
public class SealedClaimJsonMapperTest {

    @Autowired
    private SealedClaimJsonMapper mapper;

    @Test
    public void shouldMapIndividualCitizenClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().individual())
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read("/individual_rpa_case.json").trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapSoleTraderCitizenClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().soleTrader())
                .withDefendant(SampleTheirDetails.builder().soleTraderDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read("/sole_trader_rpa_case.json").trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapCompanyCitizenClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().company())
                .withDefendant(SampleTheirDetails.builder().companyDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read("/company_rpa_case.json").trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapOrganisationCitizenClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder()
                    .withCompaniesHouseNumber("09047000")
                    .organisation())
                .withDefendant(SampleTheirDetails.builder()
                    .withCompaniesHouseNumber("09047000")
                    .organisationDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read("/organisation_rpa_case.json").trim();

        //then
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }
}
