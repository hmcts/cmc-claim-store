package uk.gov.hmcts.cmc.claimstore.services.notifications.content;

public final class NotificationTemplateParameters {
    public static final String FRONTEND_BASE_URL = "frontendBaseUrl";
    public static final String RESPOND_TO_CLAIM_URL = "respondToClaimUrl";
    public static final String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    public static final String CLAIMANT_NAME = "claimantName";
    public static final String SUBMITTER_NAME = "submitterName";
    public static final String CLAIMANT_TYPE = "claimantType";
    public static final String DEFENDANT_NAME = "defendantName";
    public static final String ISSUED_ON = "issuedOn";
    public static final String RESPONSE_DEADLINE = "responseDeadline";
    public static final String EXTERNAL_ID = "externalId";
    public static final String FEES_PAID = "feesPaid";
    public static final String PIN = "pin";
    public static final String COUNTER_SIGNING_PARTY = "counterSigningParty";
    public static final String NEW_FEATURES = "newFeatures";
    public static final String DQ_DEADLINE = "DQsdeadline";
    public static final String MAIN_ADDRESS_CHANGED = "MainAddressChanged";
    public static final String PHONE_NUMBER_CHANGED = "PhoneNumberChanged";
    public static final String EMAIL_ADDRESS_CHANGED = "EmailAddressChanged";
    public static final String CORRESPONDENCE_ADDRESS_CHANGED = "CorrespondenceAddressChanged";
    public static final String PHONE_NUMBER_REMOVED = "PhoneNumberRemoved";
    public static final String CORRESPONDENCE_ADDRESS_REMOVED = "CorrespondenceAddressRemoved";
    public static final String EMAIL_ADDRESS_REMOVED = "EmailAddressRemoved";


    private NotificationTemplateParameters() {
        // Utility class
    }
}
