package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGNING_FOR_JUDGE_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGNING_FOR_LEGAL_ADVISOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFERRED_TO_MEDIATION;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.WAITING_TRANSFER;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.JUDGE_PILOT_FLAG;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.LA_PILOT_FLAG;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_JUDGE_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_LEGAL_ADVISOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_TRANSFER;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@RunWith(MockitoJUnitRunner.class)
public class DirectionsQuestionnaireServiceTest {

    private static final String NON_PILOT_COURT_NAME = "Non pilot court name";
    private static final String PILOT_COURT_NAME = "birmingham";

    private static final ResponseRejection CLAIMANT_REJECTION_PILOT;

    static {
        CLAIMANT_REJECTION_PILOT = ResponseRejection.builder()
                .freeMediation(NO)
                .directionsQuestionnaire(
                    DirectionsQuestionnaire.builder()
                        .hearingLocation(HearingLocation.builder()
                            .courtName(PILOT_COURT_NAME)
                            .build()
                        )
                        .build()
                )
                .build();
    }

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
                    .courtName(PILOT_COURT_NAME)
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
                    .courtName(PILOT_COURT_NAME)
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

    @Mock
    private PilotCourtService pilotCourtService;

    private DirectionsQuestionnaireService directionsQuestionnaireService;

    @Before
    public void setUp() {
        when(pilotCourtService.isPilotCourt(eq(PILOT_COURT_NAME), any(), any())).thenReturn(true);
        directionsQuestionnaireService = new DirectionsQuestionnaireService(pilotCourtService);
    }

    @Test
    public void shouldAssignForDirectionsIfNoFreeMediationAndDefendantIsBusinessAndClaimantCourtIsPilot() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(LA_PILOT_FLAG.getValue(), DQ_FLAG.getValue()))
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_FULL_DEFENCE_NON_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();
        Optional<CaseEvent> caseEvent =
            directionsQuestionnaireService.prepareCaseEvent(CLAIMANT_REJECTION_PILOT, claim);
        assertThat(caseEvent).contains(ASSIGNING_FOR_LEGAL_ADVISOR_DIRECTIONS);
    }

    @Test
    public void shouldPreferClaimantCourtIfDefendantIsBusiness() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();
        assertThat(directionsQuestionnaireService.getPreferredCourt(claim)).isEqualTo(PILOT_COURT_NAME);
    }

    @Test
    public void shouldPreferDefendantCourtIfDefendantIsNotBusiness() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_NON_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();
        assertThat(directionsQuestionnaireService.getPreferredCourt(claim)).isEqualTo(NON_PILOT_COURT_NAME);
    }

    @Test
    public void shouldWaitForTransferIfOnlineDqNoFreeMediationAndDefendantIsBusinessAndClaimantCourtIsNotPilot() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
            .withClaimantResponse(CLAIMANT_REJECTION_NON_PILOT)
            .withResponse(DEFENDANT_FULL_DEFENCE_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        Optional<CaseEvent> caseEvent =
            directionsQuestionnaireService.prepareCaseEvent(CLAIMANT_REJECTION_NON_PILOT, claim);
        assertThat(caseEvent).contains(WAITING_TRANSFER);
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
        assertThat(directionsQuestionnaireService.prepareCaseEvent(CLAIMANT_REJECTION_NON_PILOT, claim)).isEmpty();
    }

    @Test
    public void shouldAssignForDirectionsIfNoFreeMediationAndDefendantIsNotBusinessAndDefendantCourtIsPilot() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(LA_PILOT_FLAG.getValue(), DQ_FLAG.getValue()))
            .withClaimantResponse(CLAIMANT_REJECTION_NON_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();
        Optional<CaseEvent> caseEvent =
            directionsQuestionnaireService.prepareCaseEvent(CLAIMANT_REJECTION_NON_PILOT, claim);
        assertThat(caseEvent).contains(ASSIGNING_FOR_LEGAL_ADVISOR_DIRECTIONS);
    }

    @Test
    public void shouldAssignForJudgeDirectionsIfNoFreeMediationAndDefendantIsNotBusinessAndDefendantCourtIsPilot() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue(), JUDGE_PILOT_FLAG.getValue()))
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();
        Optional<CaseEvent> caseEvent =
            directionsQuestionnaireService.prepareCaseEvent(CLAIMANT_REJECTION_NON_PILOT, claim);
        assertThat(caseEvent).contains(ASSIGNING_FOR_JUDGE_DIRECTIONS);
    }

    @Test
    public void shouldWaitForTransferIfOnlineDqNoFreeMediationAndDefendantIsNotBusinessAndDefendantCourtIsNotPilot() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_NON_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();
        Optional<CaseEvent> caseEvent =
            directionsQuestionnaireService.prepareCaseEvent(CLAIMANT_REJECTION_NON_PILOT, claim);
        assertThat(caseEvent).contains(WAITING_TRANSFER);
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
        assertThat(directionsQuestionnaireService.prepareCaseEvent(CLAIMANT_REJECTION_PILOT, claim)).isEmpty();
    }

    @Test
    public void shouldReferToMediationIfPilotCourtAndFreeMediation() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(YES)
            .directionsQuestionnaire(
                DirectionsQuestionnaire.builder()
                    .hearingLocation(HearingLocation.builder()
                        .courtName("manchester")
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

        Optional<CaseEvent> caseEvent = directionsQuestionnaireService.prepareCaseEvent(responseRejection, claim);
        assertThat(caseEvent).contains(REFERRED_TO_MEDIATION);
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

        Optional<CaseEvent> caseEvent = directionsQuestionnaireService.prepareCaseEvent(responseRejection, claim);
        assertThat(caseEvent).contains(REFERRED_TO_MEDIATION);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsBusinessAndClaimantResponseDoesNotExist() {
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
            .withClaimantResponse(null)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        directionsQuestionnaireService.getPreferredCourt(claim);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsBusinessAndClaimantRejectionResponseHasNoDQObject() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(null)
            .build();
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
            .withClaimantResponse(responseRejection)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        directionsQuestionnaireService.getPreferredCourt(claim);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsBusinessAndClaimantResponseIsNotRejection() {
        ResponseAcceptation responseAcceptation = ResponseAcceptation.builder()
            .build();
        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
            .withClaimantResponse(responseAcceptation)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        directionsQuestionnaireService.getPreferredCourt(claim);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsIndividualAndFullDefenceDefendantResponseHasNoDQObject() {
        FullDefenceResponse defenceResponse = FullDefenceResponse.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(null)
            .build();

        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
            .withResponse(defenceResponse)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();

        directionsQuestionnaireService.getPreferredCourt(claim);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsIndividualAndPartAdmitDefendantResponseHasNoDQObject() {
        PartAdmissionResponse defenceResponse = PartAdmissionResponse.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(null)
            .build();

        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
            .withResponse(defenceResponse)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();

        directionsQuestionnaireService.getPreferredCourt(claim);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfDefendantIsIndividualAndDefendantResponseIsFullAdmission() {
        FullAdmissionResponse defenceResponse = FullAdmissionResponse.builder()
            .freeMediation(NO)
            .build();

        Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
            .withResponse(defenceResponse)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(IndividualDetails.builder().build())
                .build())
            .build();

        directionsQuestionnaireService.getPreferredCourt(claim);
    }

    @Test
    public void getDirectionsCaseStateNonPilotCourtShouldReturnReadyForTransfer() {
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(CLAIMANT_REJECTION_NON_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_NON_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        String directionsCaseState = directionsQuestionnaireService.getDirectionsCaseState(claim);
        assertThat(directionsCaseState).isEqualTo(READY_FOR_TRANSFER.getValue());
    }

    @Test
    public void getDirectionsCaseStateLAPilotCourtShouldReturnReadyForLADirections() {
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_PILOT)
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue(), LA_PILOT_FLAG.getValue()))
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        String directionsCaseState = directionsQuestionnaireService.getDirectionsCaseState(claim);
        assertThat(directionsCaseState).isEqualTo(READY_FOR_LEGAL_ADVISOR_DIRECTIONS.getValue());
    }

    @Test
    public void getDirectionsCaseStateJddoPilotCourtShouldReturnReadyForJudgeDirections() {
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_PILOT)
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue(), JUDGE_PILOT_FLAG.getValue()))
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        String directionsCaseState = directionsQuestionnaireService.getDirectionsCaseState(claim);
        assertThat(directionsCaseState).isEqualTo(READY_FOR_JUDGE_DIRECTIONS.getValue());
    }

    @Test
    public void getDirectionsCaseStatePilotCourtShouldReturnReadyForTransfer() {
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(CLAIMANT_REJECTION_PILOT)
            .withResponse(DEFENDANT_PART_ADMISSION_PILOT)
            .withClaimData(SampleClaimData
                .builder()
                .withDefendant(CompanyDetails.builder().build())
                .build())
            .build();

        String directionsCaseState = directionsQuestionnaireService.getDirectionsCaseState(claim);
        assertThat(directionsCaseState).isEqualTo(READY_FOR_TRANSFER.getValue());
    }
}
