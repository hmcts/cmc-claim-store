package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

public enum NoticeOfTransferLetterType {

    FOR_COURT("Notice of Transfer sent to court"),
    FOR_DEFENDANT("Notice of Transfer sent to defendant");

    public final String documentName;

    NoticeOfTransferLetterType(String documentName) {
        this.documentName = documentName;
    }
}
