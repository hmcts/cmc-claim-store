package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.Witness;

import java.util.Objects;
import java.util.function.Consumer;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class DirectionsQuestionnaireAssert
    extends AbstractAssert<DirectionsQuestionnaireAssert, DirectionsQuestionnaire> {
    public DirectionsQuestionnaireAssert(DirectionsQuestionnaire actual) {
        super(actual, DirectionsQuestionnaireAssert.class);
    }

    public DirectionsQuestionnaireAssert isEqualTo(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {

        isNotNull();

        isEqualToHearingLocation(ccdDirectionsQuestionnaire, actual.getHearingLocation());

        actual.getRequireSupport().ifPresent(isEqualToRequireSupport(ccdDirectionsQuestionnaire));

        actual.getWitness().ifPresent(isEqualToWitness(ccdDirectionsQuestionnaire));

        return this;
    }

    private Consumer<Witness> isEqualToWitness(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        return witness -> {
            if (!Objects.equals(witness.getSelfWitness(), ccdDirectionsQuestionnaire.getSelfWitness())) {
                failWithMessage("Expected DirectionsQuestionnaire.selfWitness to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getSelfWitness(), witness.getSelfWitness());
            }

            if (!Objects.equals(witness.getNoOfOtherWitness(), ccdDirectionsQuestionnaire.getHowManyOtherWitness())) {
                failWithMessage("Expected DirectionsQuestionnaire.noOfOtherWitness to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getHowManyOtherWitness(), witness.getNoOfOtherWitness());
            }
        };
    }

    private void isEqualToHearingLocation(
        CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire,
        HearingLocation hearingLocation
    ) {
        if (!Objects.equals(hearingLocation.getCourtName(), ccdDirectionsQuestionnaire.getHearingLocation())) {
            failWithMessage("Expected DirectionsQuestionnaire.courtName to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getLanguageInterpreted(), hearingLocation.getCourtName());
        }

        if (!Objects.equals(hearingLocation.getHearingLocationSlug(),
            ccdDirectionsQuestionnaire.getHearingLocationSlug())
        ) {
            failWithMessage("Expected DirectionsQuestionnaire.hearingLocationSlug to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getHearingLocationSlug(), hearingLocation.getHearingLocationSlug());
        }

        if (!Objects.equals(hearingLocation.getExceptionalCircumstancesReason().orElse(null),
            ccdDirectionsQuestionnaire.getExceptionalCircumstancesReason())
        ) {
            failWithMessage(
                "Expected DirectionsQuestionnaire.exceptionalCircumstances to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getExceptionalCircumstancesReason(),
                hearingLocation.getExceptionalCircumstancesReason()
            );
        }

        hearingLocation.getCourtAddress()
            .ifPresent(address -> assertThat(address).isEqualTo(ccdDirectionsQuestionnaire.getHearingCourtAddress()));
    }

    private Consumer<RequireSupport> isEqualToRequireSupport(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        return requireSupport -> {
            if (!Objects.equals(
                requireSupport.getLanguageInterpreter(),
                ccdDirectionsQuestionnaire.getLanguageInterpreted()
            )) {
                failWithMessage(
                    "Expected DirectionsQuestionnaire.languageInterpreter to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getLanguageInterpreted(), requireSupport.getLanguageInterpreter()
                );
            }

            if (!Objects.equals(
                requireSupport.getSignLanguageInterpreter(),
                ccdDirectionsQuestionnaire.getSignLanguageInterpreted()
            )) {
                failWithMessage(
                    "Expected DirectionsQuestionnaire.signLanguageInterpreter to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getSignLanguageInterpreted(), requireSupport.getSignLanguageInterpreter()
                );
            }

            if (!Objects.equals(requireSupport.getHearingLoop(), ccdDirectionsQuestionnaire.getHearingLoop())) {
                failWithMessage("Expected DirectionsQuestionnaire.hearingLoop to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getHearingLoop(), requireSupport.getHearingLoop());
            }

            if (!Objects.equals(requireSupport.getDisabledAccess(), ccdDirectionsQuestionnaire.getDisabledAccess())) {
                failWithMessage("Expected DirectionsQuestionnaire.disabledAccess to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getDisabledAccess(), requireSupport.getDisabledAccess());
            }

            if (!Objects.equals(
                requireSupport.getOtherSupport(),
                ccdDirectionsQuestionnaire.getOtherSupportRequired()
            )) {
                failWithMessage("Expected DirectionsQuestionnaire.otherSupport to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getOtherSupportRequired(), requireSupport.getOtherSupport());
            }
        };
    }
}
