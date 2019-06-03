package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.constraints.FutureDate;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.util.List;

@Builder
@Value
public class DirectionsQuestionnaire {

    @JsonUnwrapped
    private final YesNoOption selfWitness;

    private final int howManyOtherWitness;

    private final String hearingLocation;

    private final String hearingLocationSlug;

    private final String exceptionalCircumstancesReason;

    private final List<UnavailableDate> unavailableDates;

    @FutureDate
    private final LocalDate availableDate;

    private final String languageInterpreted;

    private final String signLanguageInterpreted;

    private final boolean hearingLoopSelected;

    private final boolean disabledAccessSelected;

    private final String otherSupportRequired ;

    private final List<ExpertReportRow> expertReportsRows;

    private final String expertEvidenceToExamine;

    private final String whyExpertIsNeeded;

}
