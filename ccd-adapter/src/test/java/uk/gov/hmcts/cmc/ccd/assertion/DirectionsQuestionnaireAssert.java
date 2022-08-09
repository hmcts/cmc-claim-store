package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertRequest;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.Witness;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class DirectionsQuestionnaireAssert
    extends CustomAssert<DirectionsQuestionnaireAssert, DirectionsQuestionnaire> {

    public DirectionsQuestionnaireAssert(DirectionsQuestionnaire actual) {
        super("DirectionsQuestionnaire", actual, DirectionsQuestionnaireAssert.class);
    }

    public DirectionsQuestionnaireAssert isEqualTo(CCDDirectionsQuestionnaire expected) {
        isNotNull();

        compare("courtName",
            expected.getHearingLocation(),
            actual.getHearingLocation().map(HearingLocation::getCourtName));

        compare("hearingLocationSlug",
            expected.getHearingLocationSlug(),
            actual.getHearingLocation().map(HearingLocation::getHearingLocationSlug));

        compare("exceptionalCircumstancesReason",
            expected.getExceptionalCircumstancesReason(),
            actual.getHearingLocation().flatMap(HearingLocation::getExceptionalCircumstancesReason));

        compare("hearingLocationOption",
            expected.getHearingLocationOption(), Enum::name,
            actual.getHearingLocation().map(HearingLocation::getLocationOption).map(Enum::name));

        compare("hearingCourtAddress",
            expected.getHearingCourtAddress(),
            actual.getHearingLocation().flatMap(HearingLocation::getCourtAddress),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("languageInterpreter",
            expected.getLanguageInterpreted(),
            actual.getRequireSupport().flatMap(RequireSupport::getLanguageInterpreter));

        compare("signLanguageInterpreter",
            expected.getSignLanguageInterpreted(),
            actual.getRequireSupport().flatMap(RequireSupport::getSignLanguageInterpreter));

        compare("hearingLoop",
            expected.getHearingLoop(), Enum::name,
            actual.getRequireSupport().flatMap(RequireSupport::getHearingLoop).map(Enum::name));

        compare("disabledAccess",
            expected.getDisabledAccess(), Enum::name,
            actual.getRequireSupport().flatMap(RequireSupport::getDisabledAccess).map(Enum::name));

        compare("otherSupport",
            expected.getOtherSupportRequired(),
            actual.getRequireSupport().flatMap(RequireSupport::getOtherSupport));

        compare("selfWitness",
            expected.getSelfWitness(), Enum::name,
            actual.getWitness().map(Witness::getSelfWitness).map(Enum::name));

        compare("noOfOtherWitness",
            expected.getNumberOfOtherWitnesses(),
            actual.getWitness().flatMap(Witness::getNoOfOtherWitness));

        compare("expertRequired",
            expected.getExpertRequired(), Enum::name,
            actual.getExpertRequired().map(Enum::name));

        compare("permissionForExpert",
            expected.getPermissionForExpert(), Enum::name,
            actual.getPermissionForExpert().map(Enum::name));

        compare("expertEvidenceToExamine",
            expected.getExpertEvidenceToExamine(),
            actual.getExpertRequest().map(ExpertRequest::getExpertEvidenceToExamine));

        compare("reasonForExpertAdvice",
            expected.getReasonForExpertAdvice(),
            actual.getExpertRequest().map(ExpertRequest::getReasonForExpertAdvice));

        return this;
    }

}
