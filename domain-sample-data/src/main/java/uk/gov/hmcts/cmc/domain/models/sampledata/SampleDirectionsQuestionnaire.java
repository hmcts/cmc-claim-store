package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class SampleDirectionsQuestionnaire {

    public static SampleDirectionsQuestionnaire builder() { return new SampleDirectionsQuestionnaire(); }

    public DirectionsQuestionnaire build() {
    List<LocalDate> unavailableDates = asList(LocalDate.now(), LocalDate.now());
    Map<String, LocalDate> expertReportsRowsData = new HashMap<>();
    expertReportsRowsData.put("Expert-1",LocalDate.now());

        return DirectionsQuestionnaire.builder()
            .selfWitness(YesNoOption.YES)
            .howManyOtherWitness(1)
            .exceptionalCircumstancesReason("disabled")
            .unavailableDates(unavailableDates)
            .availableDate(LocalDate.now().plusDays(1))
            .languageInterpreted("some language")
            .signLanguageInterpreted("yes")
            .hearingLoopSelected(Boolean.FALSE)
            .disabledAccessSelected(Boolean.TRUE)
            .otherSupportRequired("maybe")
            .expertReportsRows(expertReportsRowsData)
            .expertEvidenceToExamine("nothing")
            .whyExpertIsNeeded("for specified reason")
            .build();
    }
}
