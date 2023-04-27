package uk.gov.hmcts.cmc.ccd.sample.data;

import java.time.LocalDate;

public final class SampleDataConstants {

    private SampleDataConstants() {
    }

    // Claimant Data
    public static final LocalDate CLAIMANT_DOB = LocalDate.of(1950, 1, 1);
    public static final String CLAIMANT_COURT = "Claimant Court";
    public static final String CLAIMANT_EXCEPTIONAL_CIRCUMSTANCES = "As a claimant I like this court more";

    // Defendant Data
    public static final String DEFENDANT_PARTY_NAME = "Mary Richards";
    public static final String DEFENDANT_EMAIL = "defendant@email.test";
    public static final LocalDate DEFENDANT_DOB = LocalDate.of(1950, 1, 1);
    public static final String DEFENDANT_ORG_NAME = "My Org";
    public static final String DEFENDANT_ORG_PHONE_NUMBER = "07987654321";
    public static final String DEFENDANT_ORG_EMAIL = "my@email.com";
    public static final String DEFENDANT_ORG_DX_ADDRESS = "dx123";
    public static final String DEFENDANT_COURT = "Defendant Court";
    public static final String DEFENDANT_EXCEPTIONAL_CIRCUMSTANCES = "As a defendant I like this court more";
    public static final String DEFENDANT_CONTACT_PERSON = "Mr. Hyde";
    public static final String DEFENDANT_COMPANIES_HOUSE_NUMBER = "12345678";


}
