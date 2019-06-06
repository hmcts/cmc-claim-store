package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;

import java.util.Objects;

public class DirectionsQuestionnaireAssert
    extends AbstractAssert<DirectionsQuestionnaireAssert, DirectionsQuestionnaire> {
    public DirectionsQuestionnaireAssert(DirectionsQuestionnaire actual) {
        super(actual, DirectionsQuestionnaireAssert.class);
    }

    public DirectionsQuestionnaireAssert isEqualTo(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {

        isNotNull();

        if (!Objects.equals(actual.getSelfWitness().name(), ccdDirectionsQuestionnaire.getSelfWitness().name())) {
            failWithMessage("Expected self witness to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getSelfWitness(), actual.getSelfWitness());
        }

        if (!Objects.equals(actual.getHowManyOtherWitness(), ccdDirectionsQuestionnaire.getHowManyOtherWitness())) {
            failWithMessage("Expected how many other witnesses to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getHowManyOtherWitness(), actual.getHowManyOtherWitness());
        }

        if (!Objects.equals(actual.getHearingLocation(), ccdDirectionsQuestionnaire.getHearingLocation())) {
            failWithMessage("Expected hearing location to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getHearingLocation(), actual.getHearingLocation());
        }

        if (!Objects.equals(actual.getHearingLocationSlug(), ccdDirectionsQuestionnaire.getHearingLocationSlug())) {
            failWithMessage("Expected hearing location slug to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getHearingLocationSlug(), actual.getHearingLocationSlug());
        }

        if (!Objects.equals(actual.getExceptionalCircumstancesReason(),
            ccdDirectionsQuestionnaire.getExceptionalCircumstancesReason())) {
            failWithMessage("Expected exceptional circumstances reason to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getExceptionalCircumstancesReason(),
                actual.getExceptionalCircumstancesReason());
        }

        if (!Objects.equals(actual.getAvailableDate(), ccdDirectionsQuestionnaire.getAvailableDate())) {
            failWithMessage("Expected available date to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getAvailableDate(), actual.getAvailableDate());
        }

        if (!Objects.equals(actual.getLanguageInterpreted(), ccdDirectionsQuestionnaire.getLanguageInterpreted())) {
            failWithMessage("Expected language interpreted to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getLanguageInterpreted(), actual.getLanguageInterpreted());
        }

        if (!Objects.equals(actual.getSignLanguageInterpreted(),
            ccdDirectionsQuestionnaire.getSignLanguageInterpreted())) {
            failWithMessage("Expected sign language interpreted to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getSignLanguageInterpreted(), actual.getSignLanguageInterpreted());
        }

        if (!Objects.equals(actual.getHearingLoop().name(), ccdDirectionsQuestionnaire.getHearingLoop().name())) {
            failWithMessage("Expected hearing loop to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getHearingLoop(), actual.getHearingLoop());
        }

        if (!Objects.equals(actual.getDisabledAccess().name(), ccdDirectionsQuestionnaire.getDisabledAccess().name())) {
            failWithMessage("Expected disabled access to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getDisabledAccess(), actual.getDisabledAccess());
        }

        if (!Objects.equals(actual.getOtherSupportRequired(), ccdDirectionsQuestionnaire.getOtherSupportRequired())) {
            failWithMessage("Expected other support required to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getOtherSupportRequired(), actual.getOtherSupportRequired());
        }

        if (!Objects.equals(actual.getExpertEvidenceToExamine(),
            ccdDirectionsQuestionnaire.getExpertEvidenceToExamine())) {
            failWithMessage("Expected expert evidence to examine to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getExpertEvidenceToExamine(), actual.getExpertEvidenceToExamine());
        }

        if (!Objects.equals(actual.getReasonForExpertAdvice(), ccdDirectionsQuestionnaire.getReasonForExpertAdvice())) {
            failWithMessage("Expected reason for expert advice <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getReasonForExpertAdvice(), actual.getReasonForExpertAdvice());
        }

        return this;
    }
}
