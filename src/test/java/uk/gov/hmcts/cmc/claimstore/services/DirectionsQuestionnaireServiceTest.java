package uk.gov.hmcts.cmc.claimstore.services;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGN_FOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFERRED_TO_MEDIATION;
import static uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt.BIRMINGHAM;
import static uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt.MANCHESTER;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class DirectionsQuestionnaireServiceTest {

    public static final String NON_PILOT_COURT_NAME = "Non pilot court name";
    
    private DirectionsQuestionnaireService directionsQuestionnaireService;

    @Before
    public void setUp() {
        directionsQuestionnaireService = new DirectionsQuestionnaireService();
    }

    @Test
    public void shouldAssignForDirectionsIfPilotCourtAndNoFreeMediation() {
        ResponseRejection responseRejection = ResponseRejection.builder()
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

        CaseEvent caseEvent = directionsQuestionnaireService
            .prepareCaseEvent(responseRejection, BIRMINGHAM.getName()).get();
        Assertions.assertThat(caseEvent).isEqualTo(ASSIGN_FOR_DIRECTIONS);
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
            )
            .build();

        CaseEvent caseEvent = directionsQuestionnaireService
            .prepareCaseEvent(responseRejection, MANCHESTER.getName()).get();
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

        CaseEvent caseEvent = directionsQuestionnaireService
            .prepareCaseEvent(responseRejection, NON_PILOT_COURT_NAME).get();
        Assertions.assertThat(caseEvent).isEqualTo(REFERRED_TO_MEDIATION);
    }

    @Test
    public void shouldReferToMediationIfNonPilotCourtAndNoFreeMediation() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(
                DirectionsQuestionnaire.builder()
                    .hearingLocation(HearingLocation.builder()
                        .courtName(NON_PILOT_COURT_NAME)
                        .build()
                    )
                    .build()
            ).build();

        Assertions.assertThat(directionsQuestionnaireService
            .prepareCaseEvent(responseRejection, NON_PILOT_COURT_NAME)).isEmpty();
    }
}
