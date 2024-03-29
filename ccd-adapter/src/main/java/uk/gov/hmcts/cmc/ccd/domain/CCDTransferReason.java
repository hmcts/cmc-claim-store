package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDTransferReason {

    APPLICATION_GENERAL("General application"),
    APPLICATION_SET_ASIDE("Application to set aside judgment"),
    ENFORCEMENT("Enforcement"),
    DIRECTIONS("For directions"),
    HEARING("For a hearing"),
    INTERLOCUTORY_JUDGEMENT("Interlocutory Judgement"),
    JUDGES_ORDER("Judge's order"),
    LA_ORDER("Legal Advisor's order"),
    LISTING("Listing"),
    REDETERMINATION("Redetermination"),
    OTHER("Other");

    private final String value;

    CCDTransferReason(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
