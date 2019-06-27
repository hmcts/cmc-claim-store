package uk.gov.hmcts.cmc.claimstore.services;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGN_FOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFERRED_TO_MEDIATION;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class DirectionsQuestionnaireServiceTest {

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
                        .courtName(PilotCourt.BIRMINGHAM.getName())
                        .build()
                    )
                    .build()
            )
            .build();

        CaseEvent caseEvent = directionsQuestionnaireService.prepareCaseEvent(responseRejection).get();
        Assertions.assertThat(caseEvent).isEqualTo(ASSIGN_FOR_DIRECTIONS);
    }

    @Test
    public void shouldReferToMediationIfPilotCourtAndFreeMediation() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(YES)
            .directionsQuestionnaire(
                DirectionsQuestionnaire.builder()
                    .hearingLocation(HearingLocation.builder()
                        .courtName(PilotCourt.MANCHESTER.getName())
                        .build()
                    )
                    .build()
            )
            .build();

        CaseEvent caseEvent = directionsQuestionnaireService.prepareCaseEvent(responseRejection).get();
        Assertions.assertThat(caseEvent).isEqualTo(REFERRED_TO_MEDIATION);
    }

    @Test
    public void shouldReferToMediationIfNonPilotCourtAndFreeMediation() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(YES)
            .directionsQuestionnaire(
                DirectionsQuestionnaire.builder()
                    .hearingLocation(HearingLocation.builder()
                        .courtName("Non pilot court name")
                        .build()
                    )
                    .build()
            ).build();

        CaseEvent caseEvent = directionsQuestionnaireService.prepareCaseEvent(responseRejection).get();
        Assertions.assertThat(caseEvent).isEqualTo(REFERRED_TO_MEDIATION);
    }

    @Test
    public void shouldReferToMediationIfNonPilotCourtAndNoFreeMediation() {
        ResponseRejection responseRejection = ResponseRejection.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(
                DirectionsQuestionnaire.builder()
                    .hearingLocation(HearingLocation.builder()
                        .courtName("Non pilot court name")
                        .build()
                    )
                    .build()
            ).build();

        Assertions.assertThat(directionsQuestionnaireService.prepareCaseEvent(responseRejection)).isEmpty();
    }
}
