package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertRequest;
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

        actual.getExpertRequest().ifPresent(isEqualToExpertRequest(ccdDirectionsQuestionnaire));

        return this;
    }

    private Consumer<Witness> isEqualToWitness(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        return witness -> {
            if (!Objects.equals(witness.getSelfWitness().name(), ccdDirectionsQuestionnaire.getSelfWitness().name())) {
                failWithMessage("Expected DirectionsQuestionnaire.selfWitness to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getSelfWitness(), witness.getSelfWitness());
            }

            witness.getNoOfOtherWitness().ifPresent(noOfOtherWitness -> {
                    if (!Objects.equals(noOfOtherWitness, ccdDirectionsQuestionnaire.getHowManyOtherWitness())) {
                        failWithMessage(
                            "Expected DirectionsQuestionnaire.noOfOtherWitness to be <%s> but was <%s>",
                            ccdDirectionsQuestionnaire.getHowManyOtherWitness(), witness.getNoOfOtherWitness());
                    }
                }
            );
        };
    }

    private Consumer<ExpertRequest> isEqualToExpertRequest(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        return expertRequest -> {
            if (!Objects.equals(expertRequest.getExpertEvidenceToExamine(),
                ccdDirectionsQuestionnaire.getExpertEvidenceToExamine())) {
                failWithMessage("Expected DirectionsQuestionnaire.expertRequest to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getExpertEvidenceToExamine(),
                    expertRequest.getExpertEvidenceToExamine());
            }

            if (!Objects.equals(expertRequest.getReasonForExpertAdvice(),
                ccdDirectionsQuestionnaire.getReasonForExpertAdvice())) {
                failWithMessage(
                    "Expected DirectionsQuestionnaire.reasonForExpertAdvice to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getReasonForExpertAdvice(),
                    expertRequest.getReasonForExpertAdvice());
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

        if (!Objects.equals(hearingLocation.getLocationOption().name(),
            ccdDirectionsQuestionnaire.getHearingLocationOption().name())
        ) {
            failWithMessage(
                "Expected DirectionsQuestionnaire.hearingLocationOption to be <%s> but was <%s>",
                ccdDirectionsQuestionnaire.getHearingLocationOption().name(),
                hearingLocation.getLocationOption().name());
        }

        hearingLocation.getCourtAddress()
            .ifPresent(address -> assertThat(address).isEqualTo(ccdDirectionsQuestionnaire.getHearingCourtAddress()));
    }

    private Consumer<RequireSupport> isEqualToRequireSupport(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        return requireSupport -> {
            if (!Objects.equals(
                requireSupport.getLanguageInterpreter().orElse(null),
                ccdDirectionsQuestionnaire.getLanguageInterpreted()
            )) {
                failWithMessage(
                    "Expected DirectionsQuestionnaire.languageInterpreter to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getLanguageInterpreted(), requireSupport.getLanguageInterpreter()
                );
            }

            if (!Objects.equals(
                requireSupport.getSignLanguageInterpreter().orElse(null),
                ccdDirectionsQuestionnaire.getSignLanguageInterpreted()
            )) {
                failWithMessage(
                    "Expected DirectionsQuestionnaire.signLanguageInterpreter to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getSignLanguageInterpreted(), requireSupport.getSignLanguageInterpreter()
                );
            }

            requireSupport.getHearingLoop().ifPresent(hearingLoop -> {
                if (!Objects.equals(hearingLoop.name(), ccdDirectionsQuestionnaire.getHearingLoop().name())) {
                    failWithMessage("Expected DirectionsQuestionnaire.hearingLoop to be <%s> but was <%s>",
                        ccdDirectionsQuestionnaire.getHearingLoop(), hearingLoop);
                }
            });

            requireSupport.getDisabledAccess().ifPresent(disabledAccess -> {
                if (!Objects.equals(disabledAccess.name(), ccdDirectionsQuestionnaire.getDisabledAccess().name())) {
                    failWithMessage(
                        "Expected DirectionsQuestionnaire.disabledAccess to be <%s> but was <%s>",
                        ccdDirectionsQuestionnaire.getDisabledAccess(), disabledAccess);
                }
            });

            if (!Objects.equals(
                requireSupport.getOtherSupport().orElse(null),
                ccdDirectionsQuestionnaire.getOtherSupportRequired()
            )) {
                failWithMessage("Expected DirectionsQuestionnaire.otherSupport to be <%s> but was <%s>",
                    ccdDirectionsQuestionnaire.getOtherSupportRequired(), requireSupport.getOtherSupport());
            }
        };
    }
}
