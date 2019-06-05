package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.stream.Collectors;

@Component
public class DirectionsQuestionnaireMapper implements Mapper<CCDDirectionsQuestionnaire, DirectionsQuestionnaire> {

    private final ExpertRowMapper expertRowMapper;
    private final UnavailableDateMapper unavailableDateMapper;

    @Autowired
    public DirectionsQuestionnaireMapper(ExpertRowMapper expertRowMapper, UnavailableDateMapper unavailableDateMapper) {
        this.expertRowMapper = expertRowMapper;
        this.unavailableDateMapper = unavailableDateMapper;
    }

    @Override
    public CCDDirectionsQuestionnaire to(DirectionsQuestionnaire directionsQuestionnaire) {
        return CCDDirectionsQuestionnaire.builder()
            .selfWitness(CCDYesNoOption.valueOf(directionsQuestionnaire.isSelfWitness()))
            .howManyOtherWitness(directionsQuestionnaire.getHowManyOtherWitness())
            .hearingLocation(directionsQuestionnaire.getHearingLocation())
            .hearingLocationSlug(directionsQuestionnaire.getHearingLocationSlug())
            .exceptionalCircumstancesReason(directionsQuestionnaire.getExceptionalCircumstancesReason())
            .unavailableDates(directionsQuestionnaire.getUnavailableDates()
                .stream()
                .map(unavailableDateMapper::to)
                .collect(Collectors.toList()))
            .availableDate(directionsQuestionnaire.getAvailableDate())
            .languageInterpreted(directionsQuestionnaire.getLanguageInterpreted())
            .signLanguageInterpreted(directionsQuestionnaire.getSignLanguageInterpreted())
            .hearingLoop(CCDYesNoOption.valueOf(directionsQuestionnaire.isHearingLoop()))
            .disabledAccess(CCDYesNoOption.valueOf(directionsQuestionnaire.isDisabledAccess()))
            .otherSupportRequired(directionsQuestionnaire.getOtherSupportRequired())
            .expertEvidenceToExamine(directionsQuestionnaire.getExpertEvidenceToExamine())
            .expertReportsRows(directionsQuestionnaire.getExpertReportsRows()
                .stream()
                .map(expertRowMapper::to)
                .collect(Collectors.toList()))
            .reasonForExpertAdvice(directionsQuestionnaire.getReasonForExpertAdvice())
            .build();
    }

    @Override
    public DirectionsQuestionnaire from(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        return DirectionsQuestionnaire.builder()
            .selfWitness(ccdDirectionsQuestionnaire.getSelfWitness().toBoolean())
            .howManyOtherWitness(ccdDirectionsQuestionnaire.getHowManyOtherWitness())
            .hearingLocation(ccdDirectionsQuestionnaire.getHearingLocation())
            .exceptionalCircumstancesReason(ccdDirectionsQuestionnaire.getExceptionalCircumstancesReason())
            .unavailableDates(ccdDirectionsQuestionnaire.getUnavailableDates()
                .stream()
                .map(unavailableDateMapper::from)
                .collect(Collectors.toList()))
            .availableDate(ccdDirectionsQuestionnaire.getAvailableDate())
            .languageInterpreted(ccdDirectionsQuestionnaire.getLanguageInterpreted())
            .signLanguageInterpreted(ccdDirectionsQuestionnaire.getSignLanguageInterpreted())
            .hearingLoop(ccdDirectionsQuestionnaire.getHearingLoop().toBoolean())
            .disabledAccess(ccdDirectionsQuestionnaire.getDisabledAccess().toBoolean())
            .otherSupportRequired(ccdDirectionsQuestionnaire.getOtherSupportRequired())
            .expertEvidenceToExamine(ccdDirectionsQuestionnaire.getExpertEvidenceToExamine())
            .expertReportsRows(ccdDirectionsQuestionnaire.getExpertReportsRows()
                .stream()
                .map(expertRowMapper::from)
                .collect(Collectors.toList()))
            .reasonForExpertAdvice(ccdDirectionsQuestionnaire.getReasonForExpertAdvice())
            .build();
    }

}
