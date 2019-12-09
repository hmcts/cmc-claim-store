package uk.gov.hmcts.cmc.claimstore.utils;

import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGNING_FOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFERRED_TO_MEDIATION;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.WAITING_TRANSFER;
import static uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils.LA_PILOT_FLAG;
import static uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt.BIRMINGHAM;
import static uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt.MANCHESTER;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class DirectionsQuestionnaireUtilsTest {

    private static final String NON_PILOT_COURT_NAME = "Non pilot court name";

    private static final ResponseRejection CLAIMANT_REJECTION_PILOT = ResponseRejection.builder()
        .freeMediation(NO)
        .directionsQuestionnaire(
            DirectionsQuestionnaire.builder()
                .hearingLocation(HearingLocation.builder()
                    .courtName(BIRMINGHAM.getName())
                    .build()
                )
                .build()
        )
        .build();

    private static final ResponseRejection CLAIMANT_REJECTION_NON_PILOT = ResponseRejection.builder()
        .freeMediation(NO)
        .directionsQuestionnaire(
            DirectionsQuestionnaire.builder()
                .hearingLocation(HearingLocation.builder()
                    .courtName(NON_PILOT_COURT_NAME)
                    .build()
                )
                .build()
        )
        .build();

    private static final FullDefenceResponse DEFENDANT_FULL_DEFENCE_PILOT = FullDefenceResponse.builder()
        .freeMediation(NO)
        .directionsQuestionnaire(
            DirectionsQuestionnaire.builder()
                .hearingLocation(HearingLocation.builder()
                    .courtName(BIRMINGHAM.getName())
                    .build()
                )
                .build()
        )
        .build();

    private static final FullDefenceResponse DEFENDANT_FULL_DEFENCE_NON_PILOT = FullDefenceResponse.builder()
        .freeMediation(NO)
        .directionsQuestionnaire(
            DirectionsQuestionnaire.builder()
                .hearingLocation(HearingLocation.builder()
                    .courtName(NON_PILOT_COURT_NAME)
                    .build()
                )
                .build()
        )
        .build();

    private static final PartAdmissionResponse DEFENDANT_PART_ADMISSION_PILOT = PartAdmissionResponse.builder()
        .freeMediation(NO)
        .directionsQuestionnaire(
            DirectionsQuestionnaire.builder()
                .hearingLocation(HearingLocation.builder()
                    .courtName(BIRMINGHAM.getName())
                    .build()
                )
                .build()
        )
        .build();

    private static final PartAdmissionResponse DEFENDANT_PART_ADMISSION_NON_PILOT = PartAdmissionResponse.builder()
        .freeMediation(NO)
        .directionsQuestionnaire(
            DirectionsQuestionnaire.builder()
                .hearingLocation(HearingLocation.builder()
                    .courtName(NON_PILOT_COURT_NAME)
                    .build()
                )
                .build()
        )
        .build();

    @Test
    public void shouldAssignForDirectionsIfNoFreeMediationAndDefendantIsBusinessAndClaimantCourtIsPilot() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(LA_PILOT_FLAG))
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_FULL_DEFENCE_NON_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();
        CaseEvent caseEvent = DirectionsQuestionnaireUtils
            .prepareCaseEvent(CLAIMANT_REJECTION_PILOT, claim).get();
        Assertions.assertThat(caseEvent).isEqualTo(ASSIGNING_FOR_DIRECTIONS);
    }

    @Test
    public void shouldPreferClaimantCourtIfDefendantIsBusiness() {
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();
        Assertions.assertThat(DirectionsQuestionnaireUtils
            .getPreferredCourt(claim)).isEqualTo(BIRMINGHAM.getName());
    }

    @Test
    public void shouldPreferDefendantCourtIfDefendantIsNotBusiness() {
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_NON_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();
        Assertions.assertThat(DirectionsQuestionnaireUtils
            .getPreferredCourt(claim)).isEqualTo(NON_PILOT_COURT_NAME);
    }

    @Test
    public void shouldWaitForTransferIfOnlineDqNoFreeMediationAndDefendantIsBusinessAndClaimantCourtIsNotPilot() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of("directionsQuestionnaire"))
            .withClaimantResponse(CLAIMANT_REJECTION_NON_PILOT)
            .withResponse(DEFENDANT_FULL_DEFENCE_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();
        CaseEvent caseEvent = DirectionsQuestionnaireUtils
            .prepareCaseEvent(CLAIMANT_REJECTION_NON_PILOT, claim).get();
        Assertions.assertThat(caseEvent).isEqualTo(WAITING_TRANSFER);
    }

    @Test
    public void shouldReturnEmptyIfNoOnlineDqNoFreeMediationAndDefendantIsBusinessAndClaimantCourtIsNotPilot() {
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(CLAIMANT_REJECTION_NON_PILOT)
            .withResponse(DEFENDANT_FULL_DEFENCE_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();
        Assertions.assertThat(DirectionsQuestionnaireUtils
            .prepareCaseEvent(CLAIMANT_REJECTION_NON_PILOT, claim)).isEmpty();
    }

    @Test
    public void shouldAssignForDirectionsIfNoFreeMediationAndDefendantIsNotBusinessAndDefendantCourtIsPilot() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(LA_PILOT_FLAG))
            .withClaimantResponse(CLAIMANT_REJECTION_NON_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();
        CaseEvent caseEvent = DirectionsQuestionnaireUtils
            .prepareCaseEvent(CLAIMANT_REJECTION_NON_PILOT, claim).get();
        Assertions.assertThat(caseEvent).isEqualTo(ASSIGNING_FOR_DIRECTIONS);
    }

    @Test
    public void shouldWaitForTransferIfOnlineDqNoFreeMediationAndDefendantIsNotBusinessAndDefendantCourtIsNotPilot() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of("directionsQuestionnaire"))
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_NON_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();
        CaseEvent caseEvent = DirectionsQuestionnaireUtils
            .prepareCaseEvent(CLAIMANT_REJECTION_NON_PILOT, claim).get();
        Assertions.assertThat(caseEvent).isEqualTo(WAITING_TRANSFER);
    }

    @Test
    public void shouldReturnEmptyIfNoOnlineDqNoFreeMediationAndDefendantIsNotBusinessAndDefendantCourtIsNotPilot() {
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_NON_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();
        Assertions.assertThat(DirectionsQuestionnaireUtils
            .prepareCaseEvent(CLAIMANT_REJECTION_PILOT, claim)).isEmpty();
    }

    @Test
    public void shouldReferToMediationIfPilotCourtAndFreeMediation() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(YES)
            .directionsQuestionnaire(
                DirectionsQuestionnaire.builder()
                    .hearingLocation(HearingLocation.builder()
                        .courtName(MANCHESTER.getName())
                        .build()
                    )
                    .build()
            ).build();

        Claim claim = SampleClaim.builder()
            .withClaimantResponse(responseRejection)
            .withResponse(DEFENDANT_PART_ADMISSION_NON_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        CaseEvent caseEvent = DirectionsQuestionnaireUtils
            .prepareCaseEvent(responseRejection, claim).get();
        Assertions.assertThat(caseEvent).isEqualTo(REFERRED_TO_MEDIATION);
    }

    @Test
    public void shouldReferToMediationIfNonPilotCourtAndFreeMediation() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(YES)
            .directionsQuestionnaire(
                DirectionsQuestionnaire.builder()
                    .hearingLocation(HearingLocation.builder()
                        .courtName(NON_PILOT_COURT_NAME)
                        .build()
                    )
                    .build()
            ).build();

        Claim claim = SampleClaim.builder()
            .withClaimantResponse(responseRejection)
            .withResponse(DEFENDANT_PART_ADMISSION_NON_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        CaseEvent caseEvent = DirectionsQuestionnaireUtils
            .prepareCaseEvent(responseRejection, claim).get();
        Assertions.assertThat(caseEvent).isEqualTo(REFERRED_TO_MEDIATION);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsBusinessAndClaimantResponseDoesNotExist() {
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(null)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        DirectionsQuestionnaireUtils
            .getPreferredCourt(claim);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsBusinessAndClaimantRejectionResponseHasNoDQObject() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(null)
            .build();
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(responseRejection)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        DirectionsQuestionnaireUtils
            .getPreferredCourt(claim);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsBusinessAndClaimantResponseIsNotRejection() {
        ResponseAcceptation responseAcceptation = ResponseAcceptation.builder()
            .build();
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(responseAcceptation)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        DirectionsQuestionnaireUtils
            .getPreferredCourt(claim);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsIndividualAndFullDefenceDefendantResponseHasNoDQObject() {
        FullDefenceResponse defenceResponse = FullDefenceResponse.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(null)
            .build();

        Claim claim = SampleClaim.builder()
            .withResponse(defenceResponse)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();

        DirectionsQuestionnaireUtils
            .getPreferredCourt(claim);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsIndividualAndPartAdmitDefendantResponseHasNoDQObject() {
        PartAdmissionResponse defenceResponse = PartAdmissionResponse.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(null)
            .build();

        Claim claim = SampleClaim.builder()
            .withResponse(defenceResponse)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();

        DirectionsQuestionnaireUtils
            .getPreferredCourt(claim);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsIndividualAndDefendantResponseIsFullAdmission() {
        FullAdmissionResponse defenceResponse = FullAdmissionResponse.builder()
            .freeMediation(NO)
            .build();

        Claim claim = SampleClaim.builder()
            .withResponse(defenceResponse)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();

        DirectionsQuestionnaireUtils
            .getPreferredCourt(claim);
    }

    @Test
    public void shouldReturnFalseWhereClaimFeaturesAreNull() {
        assertFalse(DirectionsQuestionnaireUtils.isOnlineDQ(SampleClaim.builder().withFeatures(null).build()));
    }

    @Test
    public void shouldReturnFalseWhereClaimFeaturesAreEmpty() {
        assertFalse(DirectionsQuestionnaireUtils
            .isOnlineDQ(SampleClaim.builder().withFeatures(Collections.emptyList()).build()));
    }

    @Test
    public void shouldReturnFalseWhereClaimFeaturesDoesNotHasDirectionQuestionnaire() {
        assertFalse(DirectionsQuestionnaireUtils
            .isOnlineDQ(SampleClaim.builder().withFeatures(ImmutableList.of("admissions")).build())
        );
    }

    @Test
    public void shouldReturnTrueWhereClaimFeaturesHasDirectionQuestionnaire() {
        assertTrue(DirectionsQuestionnaireUtils
            .isOnlineDQ(SampleClaim.builder()
                .withFeatures(ImmutableList.of(DirectionsQuestionnaireUtils.DQ_FLAG))
                .build())
        );
    }
}
