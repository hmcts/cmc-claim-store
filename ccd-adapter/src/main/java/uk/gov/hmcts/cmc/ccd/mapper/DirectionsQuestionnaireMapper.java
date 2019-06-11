package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

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
        if (directionsQuestionnaire == null) {
            return null;
        }

        return CCDDirectionsQuestionnaire.builder()
            .selfWitness(mapToCCDYesNo(directionsQuestionnaire.getSelfWitness()))
            .howManyOtherWitness(directionsQuestionnaire.getHowManyOtherWitness())
            .hearingLocation(directionsQuestionnaire.getHearingLocation())
            .hearingLocationSlug(directionsQuestionnaire.getHearingLocationSlug())
            .exceptionalCircumstancesReason(directionsQuestionnaire.getExceptionalCircumstancesReason())
            .unavailableDates(asStream(directionsQuestionnaire.getUnavailableDates())
                .map(unavailableDateMapper::to)
                .collect(Collectors.toList()))
            .availableDate(directionsQuestionnaire.getAvailableDate())
            .languageInterpreted(directionsQuestionnaire.getLanguageInterpreted())
            .signLanguageInterpreted(directionsQuestionnaire.getSignLanguageInterpreted())
            .hearingLoop(mapToCCDYesNo(directionsQuestionnaire.getHearingLoop()))
            .disabledAccess(mapToCCDYesNo(directionsQuestionnaire.getDisabledAccess()))
            .otherSupportRequired(directionsQuestionnaire.getOtherSupportRequired())
            .expertEvidenceToExamine(directionsQuestionnaire.getExpertEvidenceToExamine())
            .expertReportsRows(asStream(directionsQuestionnaire.getExpertReports())
                .map(expertRowMapper::to)
                .collect(Collectors.toList()))
            .reasonForExpertAdvice(directionsQuestionnaire.getReasonForExpertAdvice())
            .build();
    }

    @Override
    public DirectionsQuestionnaire from(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        if (ccdDirectionsQuestionnaire == null) {
            return null;
        }

        return DirectionsQuestionnaire.builder()
            .selfWitness(mapFromCCDYesNo(ccdDirectionsQuestionnaire.getSelfWitness()))
            .howManyOtherWitness(ccdDirectionsQuestionnaire.getHowManyOtherWitness())
            .hearingLocation(ccdDirectionsQuestionnaire.getHearingLocation())
            .hearingLocationSlug(ccdDirectionsQuestionnaire.getHearingLocationSlug())
            .exceptionalCircumstancesReason(ccdDirectionsQuestionnaire.getExceptionalCircumstancesReason())
            .unavailableDates(asStream(ccdDirectionsQuestionnaire.getUnavailableDates())
                .map(unavailableDateMapper::from)
                .collect(Collectors.toList()))
            .availableDate(ccdDirectionsQuestionnaire.getAvailableDate())
            .languageInterpreted(ccdDirectionsQuestionnaire.getLanguageInterpreted())
            .signLanguageInterpreted(ccdDirectionsQuestionnaire.getSignLanguageInterpreted())
            .hearingLoop(mapFromCCDYesNo(ccdDirectionsQuestionnaire.getHearingLoop()))
            .disabledAccess(mapFromCCDYesNo(ccdDirectionsQuestionnaire.getDisabledAccess()))
            .otherSupportRequired(ccdDirectionsQuestionnaire.getOtherSupportRequired())
            .expertEvidenceToExamine(ccdDirectionsQuestionnaire.getExpertEvidenceToExamine())
            .expertReports(asStream(ccdDirectionsQuestionnaire.getExpertReportsRows())
                .map(expertRowMapper::from)
                .collect(Collectors.toList()))
            .reasonForExpertAdvice(ccdDirectionsQuestionnaire.getReasonForExpertAdvice())
            .build();
    }

    private CCDYesNoOption mapToCCDYesNo(YesNoOption option) {
        return option == null ? null : CCDYesNoOption.valueOf(option.name());
    }

    private YesNoOption mapFromCCDYesNo(CCDYesNoOption option) {
        return option == null ? null : YesNoOption.valueOf(option.name());
    }

}
