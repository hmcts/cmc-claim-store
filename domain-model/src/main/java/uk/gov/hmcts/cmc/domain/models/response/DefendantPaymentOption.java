package uk.gov.hmcts.cmc.domain.models.response;

public enum DefendantPaymentOption {
    FULL_BY_SPECIFIED_DATE("on or before %s"),
    INSTALMENTS("by instalments");

    String description;

    DefendantPaymentOption(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
