package uk.gov.hmcts.cmc.ccd.domain;

public enum CaseEvent {

    SUBMIT_CLAIM("submitClaimEvent"),
    DEFAULT_CCJ_REQUESTED("DefaultCCJRequested"),
    DEFENCE_SUBMITTED("DefenceSubmitted");

    private String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
