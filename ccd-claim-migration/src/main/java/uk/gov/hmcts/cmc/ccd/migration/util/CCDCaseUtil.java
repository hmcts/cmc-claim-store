package uk.gov.hmcts.cmc.ccd.migration.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;

import java.time.LocalDate;

public class CCDCaseUtil {

    private CCDCaseUtil() {
        // Utility class
    }

    public static boolean isResponded(CCDCase ccdCase) {
        return ccdCase.getRespondents()
            .stream()
            .map(CCDCollectionElement::getValue)
            .anyMatch(defendant -> defendant.getResponseSubmittedOn() != null);
    }

    public static boolean isResponseDeadlineWithinDownTime(CCDCase ccdCase) {
        return ccdCase.getRespondents()
            .stream()
            .map(CCDCollectionElement::getValue)
            .filter(respondent -> respondent.getResponseDeadline().isBefore(LocalDate.of(2019, 6, 5)))
            .anyMatch(respondent -> respondent.getResponseDeadline().isAfter(LocalDate.of(2019, 5, 29)));

    }
}
