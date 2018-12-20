package uk.gov.hmcts.cmc.ccd.deprecated;

import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.math.BigDecimal;

public class SampleData {

    //Utility class
    private SampleData() {
    }

    public static CCDResponseRejection getResponseRejection() {
        return CCDResponseRejection.builder()
            .amountPaid(BigDecimal.valueOf(123.98))
            .freeMediationOption(CCDYesNoOption.YES)
            .reason("Rejection Reason")
            .build();
    }
}
