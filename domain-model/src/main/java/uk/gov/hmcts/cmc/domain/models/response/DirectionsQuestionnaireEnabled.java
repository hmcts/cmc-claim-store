package uk.gov.hmcts.cmc.domain.models.response;

import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;

import java.util.Optional;

public interface DirectionsQuestionnaireEnabled {
    Optional<DirectionsQuestionnaire> getDirectionsQuestionnaire();
}
