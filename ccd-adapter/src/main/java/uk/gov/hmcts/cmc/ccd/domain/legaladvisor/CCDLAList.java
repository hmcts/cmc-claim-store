package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

public enum CCDLAList {
    REFER_TO_JUDGE("Any judge for direction"),
    REFER_TO_COURT_STAFF("Any courtstaff with direction"),
    L1("Legal Advisor Helen Fox"),
    L2("Legal Advisor Sandra Hutchinson"),
    L3("Legal Advisor Bushra Tabassum"),
    L4("Legal Advisor Suzanne Kind"),
    L5("Legal Advisor Ghuzunfar Hussain"),
    L6("Legal Advisor Rebecca Sutcliffe"),
    L7("Legal Advisor Rebecca Warren"),
    L8("Legal Advisor Pat Mcquade"),
    J1("Judge Adrian Bever"),
    J2("Judge Christoper Cooper"),
    J3("Judge Christopher Dodd"),
    J4("Judge Stuart Hammond"),
    J5("Judge Michael Hovington"),
    J6("Judge Margaret Langley"),
    J7("Judge Lynda Nightingale"),
    J8("Judge Stuart Hammond"),
    J9("Judge Marshall Phillips"),
    FROM_JUDGE_WITH_DIRECTION("From Judge with Direction");

    private final String value;

    CCDLAList(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

