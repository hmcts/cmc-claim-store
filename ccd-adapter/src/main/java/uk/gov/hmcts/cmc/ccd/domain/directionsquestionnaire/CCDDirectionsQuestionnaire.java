package uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.time.LocalDate;
import java.util.List;

@Builder
@Value
public class CCDDirectionsQuestionnaire {

    private final CCDYesNoOption selfWitness;

    private final int howManyOtherWitness;

    private final String hearingLocation;

    private final String hearingLocationSlug;

    private final String exceptionalCircumstancesReason;

    private final List<CCDCollectionElement<CCDUnavailableDate>> unavailableDates;

    private final LocalDate availableDate;

    private final String languageInterpreted;

    private final String signLanguageInterpreted;

    private final CCDYesNoOption hearingLoop;

    private final CCDYesNoOption disabledAccess;

    private final String otherSupportRequired;

    private final List<CCDCollectionElement<CCDExpertReportRow>> expertReportsRows;

    private final String expertEvidenceToExamine;

    private final String reasonForExpertAdvice;

}
