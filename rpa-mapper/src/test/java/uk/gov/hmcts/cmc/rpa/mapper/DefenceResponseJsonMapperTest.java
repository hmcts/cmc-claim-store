package uk.gov.hmcts.cmc.rpa.mapper;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.config.ModuleConfiguration;

import java.time.LocalDate;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings({"LineLength"})
public class DefenceResponseJsonMapperTest {

    private static String INDIVIDUAL = "/DefenceResponse/defence_response_individual_rpa_case.json";
    private static String INDIVIDUAL_MEDIATION_NOT_OPTED = "/DefenceResponse/defence_response_individual_mediation_not_opted_rpa_case.json";
    private static String INDIVIDUAL_ADDRESS_MODIFIED = "/DefenceResponse/defence_response_individual_address_modified_rpa_case.json";
    private static String SOLE_TRADER = "/DefenceResponse/defence_response_sole_trader_rpa_case.json";
    private static String COMPANY = "/DefenceResponse/defence_response_company_rpa_case.json";
    private static String ORGANISATION = "/DefenceResponse/defence_response_organisation_rpa_case.json";
    private static String ORGANISATION_ALREADY_PAID_RESPONSE = "/DefenceResponse/defence_response_organisation_rpa_case_alreadyPaid.json";
    private static final String DEFENDANT_EMAIL = "j.smith@example.com";
    @Autowired
    private DefenceResponseJsonMapper mapper;

    @Test
    public void shouldMapIndividualDefenceResponseToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(SampleResponse.FullDefence.builder().withDefendantDetails(SampleParty.builder()
                .withCorrespondenceAddress(SampleAddress.builder().withLine1("102").build()).individual()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapIndividualDefenceResponseWithMediationNotOptedToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(SampleResponse.FullDefence.builder().withMediation(YesNoOption.NO).withDefendantDetails(SampleParty.builder()
                .withCorrespondenceAddress(null).individual()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_MEDIATION_NOT_OPTED).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }


    @Test
    public void shouldMapIndividualAddressModifiedDefenceResponseToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(SampleResponse.FullDefence.builder()
                .withDefendantDetails(SampleParty.builder().withAddress(SampleAddress.builder()
                    .withPostcode("MK3 0AL").build()).withCorrespondenceAddress(null).individual()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_ADDRESS_MODIFIED).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapSoleTraderDefenceResponseToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(SampleResponse.FullDefence.builder()
                .withDefendantDetails(SampleParty.builder().withBusinessName("Sole Trading & Sons")
                    .withCorrespondenceAddress(null).soleTrader()).build())
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
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(SampleResponse.FullDefence.builder()
                .withDefendantDetails(SampleParty.builder().withCorrespondenceAddress(null).company()).build())
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
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(SampleResponse.FullDefence.builder()
                .withDefendantDetails(SampleParty.builder().withCorrespondenceAddress(null).organisation()).build())
            .withClaimData(SampleClaimData.builder().withDefendant(SampleTheirDetails.builder().organisationDetails())
                .build()).withIssuedOn(LocalDate.of(2018, 4, 26)).build();

        String expected = new ResourceReader().read(ORGANISATION).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapOrganisationAlreadyPaidDefenceResponseToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(SampleResponse.FullDefence.builder().withDefenceType(DefenceType.ALREADY_PAID)
                .withDefendantDetails(SampleParty.builder()
                    .withCorrespondenceAddress(null).organisation()).build())
            .withClaimData(SampleClaimData.builder().withDefendant(SampleTheirDetails.builder().organisationDetails())
                .build()).withIssuedOn(LocalDate.of(2018, 4, 26)).build();

        String expected = new ResourceReader().read(ORGANISATION_ALREADY_PAID_RESPONSE).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }


}
