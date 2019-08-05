package uk.gov.hmcts.cmc.domain.orders;

public enum DirectionType {
    DOCUMENTS("Upload Documents"),
    EYEWITNESS("Upload Witness Statements"),
    OTHER("Other direction"),
    EXPERT_REPORT_PERMISSION("Permission for expert report");

    private String value;

    DirectionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
