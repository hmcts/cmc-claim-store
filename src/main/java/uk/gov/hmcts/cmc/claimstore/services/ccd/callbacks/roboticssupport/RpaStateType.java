package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport;

public enum RpaStateType {
    RPA_STATE_MISSING("missing"),
    RPA_STATE_INVALID("invalid"),
    RPA_STATE_FAILED("failed"),
    RPA_STATE_SUCCEEDED("succeeded");

    private final String value;

    RpaStateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
