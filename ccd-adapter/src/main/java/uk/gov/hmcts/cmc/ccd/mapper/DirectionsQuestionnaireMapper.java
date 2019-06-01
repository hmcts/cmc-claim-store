package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReportRow;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReportRow;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class DirectionsQuestionnaireMapper implements Mapper<CCDDirectionsQuestionnaire, DirectionsQuestionnaire> {

    private final ExpertRowMapper expertRowMapper;

    @Autowired
    public DirectionsQuestionnaireMapper(ExpertRowMapper expertRowMapper) {
        this.expertRowMapper = expertRowMapper;
    }

    @Override
    public CCDDirectionsQuestionnaire to(DirectionsQuestionnaire directionsQuestionnaire) {
        return CCDDirectionsQuestionnaire
            .builder()
            .selfWitness(directionsQuestionnaire.getSelfWitness())
            .howManyOtherWitness(directionsQuestionnaire.getHowManyOtherWitness())
            .hearingLocation(directionsQuestionnaire.getHearingLocation())
            .exceptionalCircumstancesReason(directionsQuestionnaire.getExceptionalCircumstancesReason())
            .unavailableDates(directionsQuestionnaire.getUnavailableDates())
            .availableDate(directionsQuestionnaire.getAvailableDate())
            .languageInterpreted(directionsQuestionnaire.getLanguageInterpreted())
            .signLanguageInterpreted(directionsQuestionnaire.getSignLanguageInterpreted())
            .hearingLoopSelected(false)
            .disabledAccessSelected(false)
            .otherSupportRequired(directionsQuestionnaire.getOtherSupportRequired())
            .expertReportsRows(expertRowMapper.to(directionsQuestionnaire.getExpertReportsRows())
            .expertEvidenceToExamine(directionsQuestionnaire.getExpertEvidenceToExamine())
            .whyExpertIsNeeded(directionsQuestionnaire.getWhyExpertIsNeeded())
            .build()
            ;
    }

    @Override
    public DirectionsQuestionnaire from(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        return null;
    }

    private List<CCDExpertReportRow> mapExpertRowMapper(List<ExpertReportRow> expertReportRow) {
        CCDExpertReportRow.CCDExpertReportRowBuilder builder = CCDExpertReportRow

        return CCDCollectionElement.<CCDExpertReportRow>builder()
            .value()
            .build();
    }
}
