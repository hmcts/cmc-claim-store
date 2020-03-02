package uk.gov.hmcts.cmc.ccd.util;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.MediationOutcome;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.time.LocalDateTime;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDClaimSubmissionOperationIndicators.CCDClaimSubmissionOperationIndicatorsWithPinSuccess;
import static uk.gov.hmcts.cmc.ccd.util.MapperUtil.getMediationOutcome;
import static uk.gov.hmcts.cmc.ccd.util.MapperUtil.hasPaperResponse;
import static uk.gov.hmcts.cmc.ccd.util.MapperUtil.toCaseName;

public class MapperUtilTest {

    @Test
    public void caseNameNotNull() {
        Claim sampleClaim = SampleClaim.getDefault();
        String caseName = toCaseName.apply(sampleClaim);
        assertThat(caseName).isNotNull();
    }

    @Test
    public void caseNameWhenMultiClaimant() {
        Claim claimWithMultiClaimant = Claim.builder().claimData(
            SampleClaimData.builder(Arrays.asList(
                SampleParty.builder().withName("Brexiter").individual(),
                SampleParty.builder().withName("Brexiter2").individual(),
                SampleParty.builder().withName("Brexiter3").individual()),
                singletonList(
                    SampleTheirDetails.builder().withTitle("Mrs.")
                        .withFirstName("Theresa")
                        .withLastName("May")
                        .individualDetails())
            ).build()
        ).build();

        String caseName = toCaseName.apply(claimWithMultiClaimant);
        assertThat(caseName).isEqualTo("Brexiter + others Vs Mrs. Theresa May");

    }

    @Test
    public void caseNameWhenMultiDefendant() {

        Claim claimWithMultiDefendant = Claim.builder().claimData(
            SampleClaimData.builder(singletonList(SampleParty.builder().withName("Brexiter").individual()),
                Arrays.asList(
                    SampleTheirDetails.builder().withTitle("Mrs.")
                        .withFirstName("Theresa")
                        .withLastName("May")
                        .individualDetails(),
                    SampleTheirDetails.builder().withTitle("Mr.")
                        .withFirstName("John")
                        .withLastName("Bercow")
                        .individualDetails(),
                    SampleTheirDetails.builder().withTitle("Mr.")
                        .withFirstName("Claude")
                        .withLastName("Juncker")
                        .individualDetails())).build()
        ).build();

        String caseName = toCaseName.apply(claimWithMultiDefendant);
        assertThat(caseName).isEqualTo("Brexiter Vs Mrs. Theresa May + others");

    }

    @Test
    public void caseNameWhenClaimantIsSoleTrader() {

        Claim claimWithClaimantSoleTrader = Claim.builder().claimData(
            SampleClaimData.builder(
                singletonList(SampleParty.builder()
                    .withName("Georgina Hammersmith")
                    .withBusinessName("EuroStar")
                    .soleTrader()),
                singletonList(SampleTheirDetails.builder()
                    .withTitle("Mr.")
                    .withFirstName("Boris")
                    .withLastName("Johnson")
                    .individualDetails())
            ).build()
        ).build();

        String caseName = toCaseName.apply(claimWithClaimantSoleTrader);
        assertThat(caseName).isEqualTo("Georgina Hammersmith T/A EuroStar Vs Mr. Boris Johnson");

    }

    @Test
    public void caseNameWhenDefendantIsSoleTrader() {
        Claim claimWithDefendantSoleTrader = Claim.builder().claimData(
            SampleClaimData.builder(
                singletonList(SampleParty.builder()
                    .withTitle("Mrs.")
                    .withName("Boi May")
                    .individual()),
                singletonList(SampleTheirDetails.builder()
                    .withTitle("Mr.")
                    .withFirstName("Boris")
                    .withLastName("Johnson")
                    .withBusinessName("Uberflip")
                    .soleTraderDetails()
                )
            ).build()
        ).build();

        String caseName = toCaseName.apply(claimWithDefendantSoleTrader);
        assertThat(caseName).isEqualTo("Boi May Vs Mr. Boris Johnson T/A Uberflip");

    }

    @Test
    public void caseNameWhenBothAreSoleTrader() {

        Claim claimWithBothAsSoleTrader = Claim.builder().claimData(
            SampleClaimData.builder(
                singletonList(SampleParty.builder()
                    .withName("Georgina Hammersmith")
                    .withBusinessName("EuroStar")
                    .soleTrader()),
                singletonList(SampleTheirDetails.builder()
                    .withTitle("Mr.")
                    .withFirstName("Boris")
                    .withLastName("Johnson")
                    .withBusinessName("Haberdashery")
                    .soleTraderDetails())
            ).build()
        ).build();

        String caseName = toCaseName.apply(claimWithBothAsSoleTrader);
        assertThat(caseName).isEqualTo("Georgina Hammersmith T/A EuroStar Vs Mr. Boris Johnson T/A Haberdashery");

    }

    @Test
    public void caseNameWithClaimantProvidedName() {
        Claim claimWithResponse = Claim.builder().claimData(
            SampleClaimData.builder(
                singletonList(SampleParty.builder().withName("Versace").withBusinessName("Versace").soleTrader()),
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
        assertThat(caseName).isEqualTo("Versace T/A Versace Vs French Connection UK");
    }

    @Test
    public void shouldMapMediationOutcomeSuccesFromCCDCase() {
        final LocalDateTime mediationSettledTime = LocalDateTime.of(2019, 11, 13, 8, 20, 30);

        CCDCase ccdCase =
            SampleData.getCCDCitizenCaseWithRespondent(
                SampleCCDDefendant.withMediationAgreementDate(mediationSettledTime).build());

        assertThat(getMediationOutcome(ccdCase)).isEqualTo(MediationOutcome.SUCCEEDED);
    }

    @Test
    public void shouldMapMediationOutcomeFailureFromCCDCase() {
        CCDCase ccdCase =
            SampleData.getCCDCitizenCaseWithRespondent(SampleCCDDefendant.withMediationFailureReason().build());

        assertThat(getMediationOutcome(ccdCase)).isEqualTo(MediationOutcome.FAILED);
    }

    @Test
    public void shouldMapMediationOutcomeAsNullFromCCDCase() {
        CCDCase ccdCase =
            SampleData.getCCDCitizenCaseWithOperationIndicators(CCDClaimSubmissionOperationIndicatorsWithPinSuccess);

        assertThat(getMediationOutcome(ccdCase)).isNull();
    }

    @Test
    public void canContinueOnlineIfNoPaperResponse() {
        CCDCase ccdCase =
            SampleData.getCCDCitizenCaseWithOperationIndicators(CCDClaimSubmissionOperationIndicatorsWithPinSuccess);

        YesNoOption result = hasPaperResponse.apply(ccdCase);
        assertThat(result).isEqualTo(YesNoOption.NO);

    }

    @Test
    public void cantContinueOnlineIfStaffUploadedDocumentPresent() {
        CCDCase ccdCase =
            SampleData.withPaperResponseFromStaffUploadedDoc();

        YesNoOption result = hasPaperResponse.apply(ccdCase);
        assertThat(result).isEqualTo(YesNoOption.YES);

    }

    @Test
    public void cantContinueOnlineIfScannedDocumentHasResponse() {
        CCDCase ccdCase =
            SampleData.withPaperResponseFromScannedDoc();

        YesNoOption result = hasPaperResponse.apply(ccdCase);
        assertThat(result).isEqualTo(YesNoOption.YES);

    }

}
