package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class DirectionsQuestionnaireAssert
    extends AbstractAssert<DirectionsQuestionnaireAssert, DirectionsQuestionnaire> {
    public DirectionsQuestionnaireAssert(DirectionsQuestionnaire actual) {
        super(actual, DirectionsQuestionnaireAssert.class);
    }

    public DirectionsQuestionnaireAssert isEqualTo(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {

        isNotNull();

        actual.getRequireSupport().ifPresent(requireSupport -> {
            if (!Objects.equals(requireSupport.getLanguageInterpreter(), ccdDirectionsQuestionnaire.getLanguageInterpreted())) {
                failWithMessage("Expected DirectionsQuestionnaire.languageInterpreter to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getLanguageInterpreted(), requireSupport.getLanguageInterpreter());
            }

            if (!Objects.equals(requireSupport.getSignLanguageInterpreter(), ccdDirectionsQuestionnaire.getSignLanguageInterpreted())) {
                failWithMessage("Expected DirectionsQuestionnaire.signLanguageInterpreter to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getSignLanguageInterpreted(), requireSupport.getSignLanguageInterpreter());
            }

            if (!Objects.equals(requireSupport.getHearingLoop(), ccdDirectionsQuestionnaire.getHearingLoop())) {
                failWithMessage("Expected DirectionsQuestionnaire.hearingLoop to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getHearingLoop(), requireSupport.getHearingLoop());
            }

            if (!Objects.equals(requireSupport.getDisabledAccess(), ccdDirectionsQuestionnaire.getDisabledAccess())) {
                failWithMessage("Expected DirectionsQuestionnaire.disabledAccess to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getDisabledAccess(), requireSupport.getDisabledAccess());
            }

            if (!Objects.equals(requireSupport.getOtherSupport(), ccdDirectionsQuestionnaire.getOtherSupportRequired())) {
                failWithMessage("Expected DirectionsQuestionnaire.otherSupport to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getOtherSupportRequired(), requireSupport.getOtherSupport());
            }
        });

        actual.getHearingLocation().ifPresent(hearingLocation -> {
            if (!Objects.equals(hearingLocation.getCourtName(), ccdDirectionsQuestionnaire.getHearingLocation())) {
                failWithMessage("Expected DirectionsQuestionnaire.courtName to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getLanguageInterpreted(), hearingLocation.getCourtName());
            }

            if (!Objects.equals(hearingLocation.getHearingLocationSlug(), ccdDirectionsQuestionnaire.getHearingLocationSlug())) {
                failWithMessage("Expected DirectionsQuestionnaire.hearingLocationSlug to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getHearingLocationSlug(), hearingLocation.getHearingLocationSlug());
            }

            if (!Objects.equals(hearingLocation.getExceptionalCircumstancesReason().orElse(null), ccdDirectionsQuestionnaire.getExceptionalCircumstancesReason())) {
                failWithMessage("Expected DirectionsQuestionnaire.exceptionalCircumstances to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getExceptionalCircumstancesReason(), hearingLocation.getExceptionalCircumstancesReason());
            }

            assertThat(hearingLocation.getCourtAddress().orElse(null)).isEqualTo(ccdDirectionsQuestionnaire.getHearingCourtAddress());

        });

        actual.getWitness().ifPresent( witness -> {
            if (!Objects.equals(witness.getSelfWitness(), ccdDirectionsQuestionnaire.getSelfWitness())) {
                failWithMessage("Expected DirectionsQuestionnaire.courtName to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getLanguageInterpreted(), hearingLocation.getCourtName());
            }

            if (!Objects.equals(witness.getNoOfOtherWitness(), ccdDirectionsQuestionnaire.getOtherSupportRequired())) {
                failWithMessage("Expected DirectionsQuestionnaire.courtName to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getLanguageInterpreted(), hearingLocation.getCourtName());
            }
        });

        return this;
    }
}
