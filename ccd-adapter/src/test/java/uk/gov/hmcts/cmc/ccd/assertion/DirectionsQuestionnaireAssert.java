package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.domain.models.DirectionsQuestionnaire.DirectionsQuestionnaire;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DirectionsQuestionnaireAssert extends AbstractAssert<DirectionsQuestionnaireAssert, DirectionsQuestionnaire> {
    public DirectionsQuestionnaireAssert(DirectionsQuestionnaire directionsQuestionnaire) {
        super(directionsQuestionnaire, DirectionsQuestionnaire.class);
    }

    public DirectionsQuestionnaireAssert isEqualTo(DirectionsQuestionnaire directionsQuestionnaire) {

        assertThat(actual.getExceptionalCircumstancesReason()).isEqualTo(directionsQuestionnaire.getExceptionalCircumstancesReason());

        return this;
    }
}
