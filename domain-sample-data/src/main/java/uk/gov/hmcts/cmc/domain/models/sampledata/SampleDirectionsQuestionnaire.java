package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertRequest;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.VulnerabilityQuestions;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.Witness;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleHearingLocation.defaultHearingLocation;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleRequireSupport.defaultRequireSupport;

public class SampleDirectionsQuestionnaire {

    private HearingLocation hearingLocation = defaultHearingLocation;

    private RequireSupport requireSupport = defaultRequireSupport;

    private VulnerabilityQuestions vulnerabilityQuestions;

    public static SampleDirectionsQuestionnaire builder() {
        return new SampleDirectionsQuestionnaire();
    }

    public SampleDirectionsQuestionnaire withHearingLocation(HearingLocation hearingLocation) {
        this.hearingLocation = hearingLocation;
        return this;
    }

    public SampleDirectionsQuestionnaire withRequireSupport(RequireSupport requireSupport) {
        this.requireSupport = requireSupport;
        return this;
    }

    public SampleDirectionsQuestionnaire withVulnerabilityQuestions(VulnerabilityQuestions vulnerabilityQuestions) {
        this.vulnerabilityQuestions = vulnerabilityQuestions;
        return this;
    }

    public DirectionsQuestionnaire build() {
        List<UnavailableDate> unavailableDates = Collections.singletonList(
            new UnavailableDate("1", LocalDate.of(2050, 1, 1)));

        List<ExpertReport> expertReportRowsData = Collections.singletonList(
            new ExpertReport("1", "expert1", LocalDate.of(2040, 1, 1)));

        return DirectionsQuestionnaire.builder()
            .requireSupport(requireSupport)
            .expertRequired(YES)
            .permissionForExpert(YES)
            .hearingLocation(hearingLocation)
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
            .vulnerabilityQuestions(vulnerabilityQuestions)
            .build();
    }

    public DirectionsQuestionnaire buildNone() {
        List<UnavailableDate> unavailableDates = Collections.singletonList(
            new UnavailableDate("1", LocalDate.of(2050, 1, 1)));

        List<ExpertReport> expertReportRowsData = Collections.singletonList(
            new ExpertReport("1", "expert1", LocalDate.of(2040, 1, 1)));

        return DirectionsQuestionnaire.builder()
            .requireSupport(RequireSupport.builder()
                .languageInterpreter("None")
                .signLanguageInterpreter("None")
                .otherSupport("None")
                .disabledAccess(YES)
                .hearingLoop(YES)
                .build()
            )
            .expertRequired(YES)
            .permissionForExpert(YES)
            .hearingLocation(hearingLocation)
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
