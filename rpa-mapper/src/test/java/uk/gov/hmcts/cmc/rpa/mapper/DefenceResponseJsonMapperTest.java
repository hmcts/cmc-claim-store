package uk.gov.hmcts.cmc.rpa.mapper;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.config.ModuleConfiguration;

import java.util.Collections;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings({"LineLength"})
public class DefenceResponseJsonMapperTest extends BaseResponseJsonMapper {

    private static final String INDIVIDUAL = "/defence/individual_rpa_case.json";
    private static final String INDIVIDUAL_MEDIATION_NOT_OPTED = "/defence/individual_mediation_not_opted_rpa_case.json";
    private static final String INDIVIDUAL_ADDRESS_MODIFIED = "/defence/individual_address_modified_rpa_case.json";
    private static final String SOLE_TRADER = "/defence/sole_trader_rpa_case.json";
    private static final String COMPANY = "/defence/company_rpa_case.json";
    private static final String ORGANISATION = "/defence/organisation_rpa_case.json";
    private static final String ORGANISATION_ALREADY_PAID_RESPONSE = "/defence/organisation_rpa_case_alreadyPaid.json";
    private static final String HEARING_REQUIREMENTS = "/defence/hearing_requirements_case.json";

    @Autowired
    private DefenceResponseJsonMapper mapper;

    @Test
    public void shouldMapIndividualDefenceResponseToRPA() throws JSONException {
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(SampleResponse.FullDefence.builder().withDefendantDetails(SampleParty.builder()
                .withCorrespondenceAddress(SampleAddress.builder().line1("102").build()).individual()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapIndividualDefenceResponseWithMediationNotOptedToRPA() throws JSONException {
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(SampleResponse.FullDefence.builder().withMediation(YesNoOption.NO).withDefendantDetails(SampleParty.builder()
                .withCorrespondenceAddress(null).individual()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_MEDIATION_NOT_OPTED).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapIndividualAddressModifiedDefenceResponseToRPA() throws JSONException {
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(SampleResponse.FullDefence.builder()
                .withDefendantDetails(SampleParty.builder().withAddress(SampleAddress.builder()
                    .postcode("MK3 0AL").build()).withCorrespondenceAddress(null).individual()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_ADDRESS_MODIFIED).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapSoleTraderDefenceResponseToRPA() throws JSONException {
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(SampleResponse.FullDefence.builder()
                .withDefendantDetails(SampleParty.builder().withBusinessName("Sole Trading & Sons")
                    .withCorrespondenceAddress(null).soleTrader()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().soleTraderDetails())
                .build())
            .build();

        String expected = new ResourceReader().read(SOLE_TRADER).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapCompanyDefenceResponseToRPA() throws JSONException {
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(SampleResponse.FullDefence.builder()
                .withDefendantDetails(SampleParty.builder().withCorrespondenceAddress(null).company()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().companyDetails())
                .build())
            .build();

        String expected = new ResourceReader().read(COMPANY).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapOrganisationDefenceResponseToRPA() throws JSONException {
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(SampleResponse.FullDefence.builder()
                .withDefendantDetails(SampleParty.builder().withCorrespondenceAddress(null).organisation()).build())
            .withClaimData(SampleClaimData.builder().withDefendant(SampleTheirDetails.builder().organisationDetails())
                .build())
            .build();

        String expected = new ResourceReader().read(ORGANISATION).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapHearingRequirementsToRPAWhenDQFeatureIsEnabled() throws JSONException {

        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(SampleResponse.FullDefence.builder()
                .withDirectionsQuestionnaire(DirectionsQuestionnaire.builder()
                    .requireSupport(RequireSupport.builder().disabledAccess(YesNoOption.YES).build()).build())
                .withDefendantDetails(SampleParty.builder()
                    .withCorrespondenceAddress(SampleAddress.builder().line1("102").build()).individual()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withFeatures(Collections.singletonList(DQ_FLAG.getValue()))
            .build();

        String expected = new ResourceReader().read(HEARING_REQUIREMENTS).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapHearingRequirementsToFalseWhenDQFeatureIsEnabledAndNoDQIsPresent() throws JSONException {
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(SampleResponse.FullDefence.builder()
                .withDirectionsQuestionnaire(null)
                .withDefendantDetails(SampleParty.builder()
                    .withCorrespondenceAddress(SampleAddress.builder().line1("102").build()).individual()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withFeatures(Collections.singletonList(DQ_FLAG.getValue()))
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldNotMapHearingRequirementsToRPAWhenDQFeatureIsDisabled() throws JSONException {
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(SampleResponse.FullDefence.builder()
                .withDirectionsQuestionnaire(DirectionsQuestionnaire.builder()
                    .requireSupport(RequireSupport.builder().disabledAccess(YesNoOption.YES).build()).build())
                .withDefendantDetails(SampleParty.builder()
                    .withCorrespondenceAddress(SampleAddress.builder().line1("102").build()).individual()).build())
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL).trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

}
