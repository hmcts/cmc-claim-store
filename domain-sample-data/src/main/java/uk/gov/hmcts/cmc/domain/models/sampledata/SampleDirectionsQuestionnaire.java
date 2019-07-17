package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertRequest;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.Witness;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class SampleDirectionsQuestionnaire {

    public static SampleDirectionsQuestionnaire builder() {
        return new SampleDirectionsQuestionnaire();
    }

    public DirectionsQuestionnaire build() {
        List<UnavailableDate> unavailableDates = asList(
            new UnavailableDate("1", LocalDate.of(2050, 1, 1)));

        List<ExpertReport> expertReportRowsData = asList(
            new ExpertReport("1", "expert1", LocalDate.of(2040, 1, 1)));

        return DirectionsQuestionnaire.builder()
            .requireSupport(RequireSupport.builder()
                .languageInterpreter("English")
                .signLanguageInterpreter("Need Sign Language")
                .disabledAccess(YES)
                .hearingLoop(NO)
                .build()
            )
            .hearingLocation(SampleHearingLocation.defaultHearingLocation.get()
            )
            .expertRequest(ExpertRequest.builder()
                .reasonForExpertAdvice("A valid reason")
                .expertEvidenceToExamine("Evidence to examine")
                .build()
            )
            .witness(Witness.builder()
                .selfWitness(YES)
                .noOfOtherWitness(1)
                .build()
            )
            .unavailableDates(unavailableDates)
            .expertReports(expertReportRowsData)
            .build();
    }
}
