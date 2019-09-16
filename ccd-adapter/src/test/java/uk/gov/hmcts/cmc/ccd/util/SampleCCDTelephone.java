package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;

public final class SampleCCDTelephone {

    private SampleCCDTelephone() {
        //Do nothing constructor
    }

    public static CCDTelephone withDefaultPhoneNumber() {
        return CCDTelephone.builder().telephoneNumber("0776655443322").build();
    }

}
