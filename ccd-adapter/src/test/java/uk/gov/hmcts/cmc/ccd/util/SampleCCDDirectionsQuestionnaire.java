package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReport;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;

public class SampleCCDDirectionsQuestionnaire {

    public static SampleCCDDirectionsQuestionnaire builder() {
        return new SampleCCDDirectionsQuestionnaire();
    }

    public CCDDirectionsQuestionnaire build() {

//        List<CCDCollectionElement<CCDUnavailableDate>> unavailableDates = asList(
//            CCDCollectionElement.<CCDUnavailableDate>builder()
//                .value(CCDUnavailableDate
//                    .builder()
//                    .unavailableDate(LocalDate.of(2050, 1, 1))
//                    .build())
//                .build()
//        );

        List<CCDCollectionElement<CCDExpertReport>> expertReportRow = asList(
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
            .howManyOtherWitness(1)
            .hearingLocation("London")
            .hearingLocationSlug("London-Court")
            .exceptionalCircumstancesReason("disabled")
            .unavailableDates(null)
            .languageInterpreted("some language")
            .signLanguageInterpreted("some sign language")
            .hearingLoop(NO)
            .disabledAccess(YES)
            .otherSupportRequired("maybe")
            .expertReports(expertReportRow)
            .expertEvidenceToExamine("nothing")
            .reasonForExpertAdvice("for specified reason")
            .build();
    }
}
