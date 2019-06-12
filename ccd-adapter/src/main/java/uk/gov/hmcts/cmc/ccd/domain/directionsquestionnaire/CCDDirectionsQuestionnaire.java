package uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.time.LocalDate;
import java.util.List;

@Builder
@Value
public class CCDDirectionsQuestionnaire {

    private String languageInterpreted;
    private String signLanguageInterpreted;
    private CCDYesNoOption hearingLoop;
    private CCDYesNoOption disabledAccess;
    private String otherSupportRequired;

    private String hearingLocation;
    private String hearingLocationSlug;
    private CCDCourtLocationOption hearingLocationOption;
    private CCDAddress hearingCourtAddress;
    private String exceptionalCircumstancesReason;

    private CCDYesNoOption selfWitness;
    private int howManyOtherWitness;

    private List<CCDCollectionElement<CCDExpertReport>> expertReports;

    private List<CCDCollectionElement<LocalDate>> unavailableDates;

    private String expertEvidenceToExamine;
    private String reasonForExpertAdvice;
}
