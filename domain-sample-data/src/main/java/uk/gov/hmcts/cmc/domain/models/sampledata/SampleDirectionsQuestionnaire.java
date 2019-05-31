package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReportRow;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;

public class SampleDirectionsQuestionnaire {

    public static SampleDirectionsQuestionnaire builder() { return new SampleDirectionsQuestionnaire(); }

    public DirectionsQuestionnaire build() {
    List<LocalDate> unavailableDates = asList(LocalDate.of(2050,1,1));
    List<ExpertReportRow> expertReportRowsData = asList(new ExpertReportRow("expert1", LocalDate.of(2040,1,1)));

        return DirectionsQuestionnaire.builder()
            .selfWitness(YesNoOption.YES)
            .howManyOtherWitness(1)
            .hearingLocation("London")
            .hearingLocationSlug("London-Court")
            .exceptionalCircumstancesReason("disabled")
            .unavailableDates(unavailableDates)
            .availableDate(LocalDate.of(2050,1,2))
            .languageInterpreted("some language")
            .signLanguageInterpreted("yes")
            .hearingLoopSelected(Boolean.FALSE)
            .disabledAccessSelected(Boolean.TRUE)
            .otherSupportRequired("maybe")
            .expertReportsRows(expertReportRowsData)
            .expertEvidenceToExamine("nothing")
            .whyExpertIsNeeded("for specified reason")
            .build();
    }
}
