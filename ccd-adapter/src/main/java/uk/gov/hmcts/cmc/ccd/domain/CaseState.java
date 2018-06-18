package uk.gov.hmcts.cmc.ccd.domain;

public enum CaseState {
    ONHOLD("onhold"),
    CLOSED("closed"),
    OPEN("open");

    private final String state;

    CaseState(String state) {
        this.state = state;
    }

    public String getValue() {
        return state;
    }
}
