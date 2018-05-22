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
    private static String INDIVIDUAL = "/DefenceResponse/defence_response_individual_rpa_case.json";
    private static String SOLE_TRADER = "/DefenceResponse/defence_response_sole_trader_rpa_case.json";
    private static String COMPANY = "/DefenceResponse/defence_response_company_rpa_case.json";
    private static String ORGANISATION = "/DefenceResponse/defence_response_organisation_rpa_case.json";

    @Test
    public void shouldMapIndividualDefenceResponseToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapSoleTraderDefenceResponseToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().soleTraderDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read(SOLE_TRADER).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapCompanyDefenceResponseToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().companyDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read(COMPANY).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapOrganisationDefenceResponseToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder()
                    .withCompaniesHouseNumber("09047000")
                    .organisationDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))

            .build();

        String expected = new ResourceReader().read(ORGANISATION).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }
}
