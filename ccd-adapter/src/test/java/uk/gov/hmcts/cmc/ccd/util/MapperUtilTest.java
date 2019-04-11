package uk.gov.hmcts.cmc.ccd.util;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class MapperUtilTest {

    @Test
    public void caseNameNotNull() {
        Claim sampleClaim = SampleClaim.getDefault();
        String caseName = MapperUtil.toCaseName.apply(sampleClaim);
        assertNotNull(caseName);
    }

    @Test
    public void caseNameWhenMultiClaimant() {
        Claim claimWithMultiClaimant = Claim.builder().claimData(
            SampleClaimData.builder(Arrays.asList(
                SampleParty.builder().withName("Brexiter").individual(),
                SampleParty.builder().withName("Brexiter2").individual(),
                SampleParty.builder().withName("Brexiter3").individual()),
                singletonList(
                    SampleTheirDetails.builder().withTitle("Mrs.").
                        withFirstName("Theresa")
                        .withLastName("May")
                        .individualDetails())
            ).build()
        ).build();

        String caseName = MapperUtil.toCaseName.apply(claimWithMultiClaimant);
        assertNotNull(caseName);
        assertThat(caseName, is("Brexiter + others Vs Mrs. Theresa May"));

    }

    @Test
    public void caseNameWhenMultiDefendant() {

        Claim claimWithMultiDefendant = Claim.builder().claimData(
            SampleClaimData.builder(singletonList(SampleParty.builder().withName("Brexiter").individual()),
                Arrays.asList(
                    SampleTheirDetails.builder().withTitle("Mrs.").
                        withFirstName("Theresa")
                        .withLastName("May")
                        .individualDetails(),
                    SampleTheirDetails.builder().withTitle("Mr.").
                        withFirstName("John")
                        .withLastName("Bercow")
                        .individualDetails(),
                    SampleTheirDetails.builder().withTitle("Mr.").
                        withFirstName("Claude")
                        .withLastName("Juncker")
                        .individualDetails())).build()
        ).build();

        String caseName = MapperUtil.toCaseName.apply(claimWithMultiDefendant);
        assertNotNull(caseName);
        assertThat(caseName, is("Brexiter Vs Mrs. Theresa May + others"));

    }

    @Test
    public void caseNameWhenClaimantIsNotIndividual() {

        Claim claimWithMultiDefendant = Claim.builder().claimData(
            SampleClaimData.builder(
                singletonList(SampleParty.builder().withName("Euro Star").soleTrader()),
                singletonList(SampleTheirDetails.builder()
                    .withTitle("Mr.")
                    .withFirstName("Boris")
                    .withLastName("Johnson")
                    .individualDetails())
            ).build()
        ).build();

        String caseName = MapperUtil.toCaseName.apply(claimWithMultiDefendant);
        assertNotNull(caseName);
        assertThat(caseName, is("Euro Star Vs Mr. Boris Johnson"));

    }

    @Test
    public void caseNameWhenDefendantIsNotIndividual() {
        Claim claimWithMultiDefendant = Claim.builder().claimData(
            SampleClaimData.builder(
                singletonList(SampleParty.builder().withName("Euro Star").soleTrader()),
                singletonList(SampleTheirDetails.builder().withName("Boris Johnson").companyDetails())
            ).build()
        ).build();

        String caseName = MapperUtil.toCaseName.apply(claimWithMultiDefendant);
        assertNotNull(caseName);
        assertThat(caseName, is("Euro Star Vs Boris Johnson"));

    }

    @Test
    public void caseNameWithClaimantProvidedName() {
        Claim claimWithResponse = Claim.builder().claimData(
            SampleClaimData.builder(
                singletonList(SampleParty.builder().withName("Versace").soleTrader()),
                singletonList(SampleTheirDetails.builder().withName("FCUK").companyDetails())
            ).build()
        ).response(PartAdmissionResponse
            .builder()
            .defendant(SampleParty
                .builder()
                .withName("French Connection UK")
                .soleTrader()).build())
            .build();

        String caseName = MapperUtil.toCaseName.apply(claimWithResponse);
        assertNotNull(caseName);
        assertThat(caseName, is("Versace Vs French Connection UK"));
    }

}
