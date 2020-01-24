package uk.gov.hmcts.cmc.ccd.sample.data;

import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDCourtLocationOption;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReport;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;

public class SampleCCDDirectionsQuestionnaire {

    public static SampleCCDDirectionsQuestionnaire builder() {
        return new SampleCCDDirectionsQuestionnaire();
    }

    public CCDDirectionsQuestionnaire build() {

        List<CCDCollectionElement<LocalDate>> unavailableDates = Collections.singletonList(
            CCDCollectionElement.<LocalDate>builder()
                .value(LocalDate.of(2050, 1, 1))
                .build()
        );

        List<CCDCollectionElement<CCDExpertReport>> expertReportRow = Collections.singletonList(
            CCDCollectionElement.<CCDExpertReport>builder()
                .value(CCDExpertReport
                    .builder()
                    .expertName("expert1")
                    .expertReportDate(LocalDate.of(2040, 1, 1))
                    .build())
                .build()
        );

        return CCDDirectionsQuestionnaire.builder()
            .selfWitness(YES)
            .numberOfOtherWitnesses(1)
            .hearingLocation("London")
            .hearingLocationSlug("London-Court")
            .hearingLocationOption(CCDCourtLocationOption.ALTERNATE_COURT)
            .exceptionalCircumstancesReason("disabled")
            .unavailableDates(unavailableDates)
            .languageInterpreted("some language")
            .signLanguageInterpreted("some sign language")
            .hearingLoop(NO)
            .disabledAccess(YES)
            .otherSupportRequired("maybe")
            .expertReports(expertReportRow)
            .expertRequired(NO)
            .permissionForExpert(NO)
            .expertEvidenceToExamine("nothing")
            .reasonForExpertAdvice("for specified reason")
            .build();
    }
}
