package uk.gov.hmcts.cmc.rpa.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepresentative;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.config.ModuleConfiguration;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class LegalSealedClaimJsonMapperTest {

    @Autowired
    private LegalSealedClaimJsonMapper mapper;

    @Test
    public void shouldMapIndividualLegalClaimToRPA() throws JSONException, JsonProcessingException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withExternalId(SampleClaim.RAND_UUID)
                .withExternalReferenceNumber("LBA/UM1616668")
                .withAmount(SampleAmountRange.builder().build())
                .clearDefendants()
                .clearClaimants()
                .withClaimant(SampleParty.builder()
                    .withPhone("(0)207 127 0000")
                    .withRepresentative(SampleRepresentative.builder()
                        .organisationContactDetails(ContactDetails.builder()
                            .dxAddress("xyzrt")
                            .email("abn@gmail.com")
                            .phone("01123456789")
                            .build())
                        .build())
                    .individual())
                .withDefendant(SampleTheirDetails.builder()
                    .withRepresentative(SampleRepresentative.builder()
                        .organisationContactDetails(ContactDetails.builder()
                            .dxAddress("xyzrt")
                            .email("abn@gmail.com")
                            .phone("01123456789")
                            .build())
                        .build())
                    .individualDetails())
                .build()
            )
            .withReferenceNumber("006LR003")
            .build();

        String expected = new ResourceReader().read("/claim/individual_rpa_legal_case.json").trim();

        String actualStr = mapper.map(claim).toString();
        assertEquals(expected, actualStr, STRICT);
    }

    @Test
    public void shouldMapSoleTraderLegalClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withExternalId(SampleClaim.RAND_UUID)
                .withExternalReferenceNumber("LBA/UM1616668")
                .withAmount(SampleAmountRange.builder().build())
                .clearDefendants()
                .clearClaimants()
                .withClaimant(SampleParty.builder()
                    .withPhone("(0)207 127 0000")
                    .withRepresentative(SampleRepresentative.builder()
                        .organisationContactDetails(ContactDetails.builder()
                            .dxAddress("xyzrt")
                            .email("abn@gmail.com")
                            .phone("01123456789")
                            .build())
                        .build())
                    .soleTrader())
                .withDefendant(SampleTheirDetails.builder()
                    .withRepresentative(SampleRepresentative.builder()
                        .organisationContactDetails(ContactDetails.builder()
                            .dxAddress("xyzrt")
                            .email("abn@gmail.com")
                            .phone("01123456789")
                            .build())
                        .build())
                    .soleTraderDetails())
                .build()
            )
            .withReferenceNumber("006LR003")
            .build();

        String expected = new ResourceReader().read("/claim/sole_trader_rpa_legal_case.json").trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapCompanyLegalClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withExternalId(SampleClaim.RAND_UUID)
                .withExternalReferenceNumber("LBA/UM1616668")
                .withAmount(SampleAmountRange.builder().build())
                .clearDefendants()
                .clearClaimants()
                .withClaimant(SampleParty.builder()
                    .withPhone("(0)207 127 0000")
                    .withRepresentative(SampleRepresentative.builder()
                        .organisationContactDetails(ContactDetails.builder()
                            .dxAddress("xyzrt")
                            .email("abn@gmail.com")
                            .phone("01123456789")
                            .build())
                        .build())
                    .company())
                .withDefendant(SampleTheirDetails.builder()
                    .withRepresentative(SampleRepresentative.builder()
                        .organisationContactDetails(ContactDetails.builder()
                            .dxAddress("xyzrt")
                            .email("abn@gmail.com")
                            .phone("01123456789")
                            .build())
                        .build())
                    .companyDetails())
                .build()
            )
            .withReferenceNumber("006LR003")
            .build();

        String expected = new ResourceReader().read("/claim/company_rpa_legal_case.json").trim();

        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapOrganisationLegalClaimToRPA() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withExternalId(SampleClaim.RAND_UUID)
                .withExternalReferenceNumber("LBA/UM1616668")
                .withAmount(SampleAmountRange.builder().build())
                .clearDefendants()
                .clearClaimants()
                .withClaimant(SampleParty.builder()
                    .withPhone("(0)207 127 0000")
                    .withRepresentative(SampleRepresentative.builder()
                        .organisationContactDetails(ContactDetails.builder()
                            .dxAddress("xyzrt")
                            .email("abn@gmail.com")
                            .phone("01123456789")
                            .build())
                        .build())
                    .organisation())
                .withDefendant(SampleTheirDetails.builder()
                    .withRepresentative(SampleRepresentative.builder()
                        .organisationContactDetails(ContactDetails.builder()
                            .dxAddress("xyzrt")
                            .email("abn@gmail.com")
                            .phone("01123456789")
                            .build())
                        .build())
                    .organisationDetails())
                .build()
            )
            .withReferenceNumber("006LR003")
            .build();

        String expected = new ResourceReader().read("/claim/organisation_rpa_legal_case.json").trim();

        //then
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }
}
