package uk.gov.hmcts.cmc.rpa.mapper;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.config.ModuleConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SealedClaimJsonMapperTest {

    @Autowired
    private SealedClaimJsonMapper mapper;

    private final LocalDate issueDate = LocalDate.of(2018, 4, 26);
    private final Interest interest = SampleInterest.builder()
        .withInterestDate(
            new InterestDate(InterestDate.InterestDateType.CUSTOM,
                issueDate.minusDays(101),
                "reason",
                InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT))
        .withType(Interest.InterestType.STANDARD)
        .withRate(new BigDecimal(8))
        .build();

    @Test
    public void shouldMapIndividualCitizenClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().individual())
                .withDefendant(SampleTheirDetails.builder()
                    .withDateOfBirth(null)
                    .withServiceAddress(null)
                    .individualDetails())
                .withInterest(interest)
                .build())
            .withIssuedOn(issueDate)
            .build();

        String expected = new ResourceReader().read("/claim/individual_rpa_case.json").trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapSoleTraderCitizenClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().withBusinessName("AutoTraders").soleTrader())
                .withDefendant(SampleTheirDetails.builder().withBusinessName("RoboticsTraders")
                    .withDateOfBirth(null)
                    .withServiceAddress(null)
                    .soleTraderDetails())
                .withInterest(interest)
                .build())
            .withIssuedOn(issueDate)
            .build();

        String expected = new ResourceReader().read("/claim/sole_trader_rpa_case.json").trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapCompanyCitizenClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder().company())
                .withDefendant(SampleTheirDetails.builder()
                    .withDateOfBirth(null)
                    .withServiceAddress(null)
                    .companyDetails())
                .withInterest(interest)
                .build())
            .withIssuedOn(issueDate)
            .build();

        String expected = new ResourceReader().read("/claim/company_rpa_case.json").trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapOrganisationCitizenClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withClaimant(SampleParty.builder()
                    .organisation())
                .withDefendant(SampleTheirDetails.builder()
                    .withDateOfBirth(null)
                    .withServiceAddress(null)
                    .organisationDetails())
                .withInterest(interest)
                .build())
            .withIssuedOn(issueDate)
            .build();

        String expected = new ResourceReader().read("/claim/organisation_rpa_case.json").trim();

        //then
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }
}
