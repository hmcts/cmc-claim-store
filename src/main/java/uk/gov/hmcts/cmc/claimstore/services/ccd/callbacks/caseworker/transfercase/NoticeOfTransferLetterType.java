package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

public enum NoticeOfTransferLetterType {

    FOR_COURT("Notice of Transfer sent to court"),
    TO_COURT_FOR_DEFENDANT("Notice of Transfer to Court sent to defendant"),
    TO_CCBC_FOR_DEFENDANT("Notice of Transfer To CCBC sent to defendant");

    public final String documentName;

    NoticeOfTransferLetterType(String documentName) {
        this.documentName = documentName;
    }
}
