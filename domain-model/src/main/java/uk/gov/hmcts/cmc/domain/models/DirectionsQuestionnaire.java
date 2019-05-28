package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.constraints.FutureDate;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class DirectionsQuestionnaire {

    @JsonUnwrapped
    private final YesNoOption selfWitness;

    private final int howManyOtherWitness;

    private final String hearingLocation;

    private final String exceptionalCircumstancesReason;

    private final List<LocalDate> unavailableDates;

    @FutureDate
    private final LocalDate availableDate;

    private final String languageInterpreted;

    private final String signLanguageInterpreted;

    private final boolean hearingLoopSelected;

    private final boolean disabledAccessSelected;

    private final String otherSupportRequired ;

    private final Map<String, LocalDate> expertReportsRows;

    private final String expertEvidenceToExamine;

    private final String whyExpertIsNeeded;

}
