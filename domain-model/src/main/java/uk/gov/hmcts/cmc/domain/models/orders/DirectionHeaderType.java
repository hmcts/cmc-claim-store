package uk.gov.hmcts.cmc.domain.models.orders;

public enum DirectionHeaderType {
    UPLOAD("Upload"),
    CONFIRM("Confirm");

    private String value;

    DirectionHeaderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
