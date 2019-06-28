package uk.gov.hmcts.cmc.claimstore.utils;

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

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGNING_FOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFERRED_TO_MEDIATION;
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
    public void shouldReturnEmptyIfNoFreeMediationAndDefendantIsBusinessAndClaimantCourtIsNotPilot() {
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
    public void shouldReturnEmptyIfNoFreeMediationAndDefendantIsNotBusinessAndDefendantCourtIsNotPilot() {
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

    @Test(expected =  IllegalStateException.class)
    public void shouldThrowIfDefendantIsBusinessAndClaimantResponseDoesNotExist() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(null)
            .build();
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(null)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        DirectionsQuestionnaireUtils
            .prepareCaseEvent(responseRejection, claim);
    }

    @Test(expected =  IllegalStateException.class)
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
            .prepareCaseEvent(responseRejection, claim);
    }

    @Test(expected =  IllegalStateException.class)
    public void shouldThrowIfDefendantIsBusinessAndClaimantResponseIsNotRejection() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(null)
            .build();
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
            .prepareCaseEvent(responseRejection, claim);
    }

    @Test(expected =  IllegalStateException.class)
    public void shouldThrowIfDefendantIsIndividualAndFullDefenceDefendantResponseHasNoDQObject() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(NO)
            .build();

        FullDefenceResponse defenceResponse = FullDefenceResponse.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(null)
            .build();

        Claim claim = SampleClaim.builder()
            .withClaimantResponse(responseRejection)
            .withResponse(defenceResponse)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();

        DirectionsQuestionnaireUtils
            .prepareCaseEvent(responseRejection, claim);
    }

    @Test(expected =  IllegalStateException.class)
    public void shouldThrowIfDefendantIsIndividualAndPartAdmitDefendantResponseHasNoDQObject() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(NO)
            .build();

        PartAdmissionResponse defenceResponse = PartAdmissionResponse.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(null)
            .build();

        Claim claim = SampleClaim.builder()
            .withClaimantResponse(responseRejection)
            .withResponse(defenceResponse)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();

        DirectionsQuestionnaireUtils
            .prepareCaseEvent(responseRejection, claim);
    }

    @Test(expected =  IllegalStateException.class)
    public void shouldThrowIfDefendantIsIndividualAndDefendantResponseIsFullAdmission() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(NO)
            .build();

        FullAdmissionResponse defenceResponse = FullAdmissionResponse.builder()
            .freeMediation(NO)
            .build();

        Claim claim = SampleClaim.builder()
            .withClaimantResponse(responseRejection)
            .withResponse(defenceResponse)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();

        DirectionsQuestionnaireUtils
            .prepareCaseEvent(responseRejection, claim);
    }
}
