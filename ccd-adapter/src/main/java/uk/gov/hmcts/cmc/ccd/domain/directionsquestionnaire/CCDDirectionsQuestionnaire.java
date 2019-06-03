package uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class CCDDirectionsQuestionnaire {

    @JsonUnwrapped
    private final YesNoOption selfWitness;

    private final int howManyOtherWitness;

    private final String hearingLocation;

    private final String hearingLocationSlug;

    private final String exceptionalCircumstancesReason;

    private final List<CCDCollectionElement<CCDUnavailableDate>> unavailableDates;

    private final LocalDate availableDate;

    private final String languageInterpreted;

    private final String signLanguageInterpreted;

    private final boolean hearingLoopSelected;

    private final boolean disabledAccessSelected;

    private final String otherSupportRequired ;

    private final List<CCDCollectionElement<CCDExpertReportRow>> expertReportsRows;

    private final String expertEvidenceToExamine;

    private final String whyExpertIsNeeded;

}
