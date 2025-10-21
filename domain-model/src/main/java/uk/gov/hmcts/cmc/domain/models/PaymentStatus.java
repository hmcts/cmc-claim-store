package uk.gov.hmcts.cmc.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    INITIATED("Initiated"),
    SUCCESS("Success"),
    FAILED("Failed"),
    PENDING("Pending"),
    DECLINED("Declined");

    private String status;

    public static PaymentStatus fromValue(String value) {
        return Arrays.stream(PaymentStatus.values())
            .filter(val -> val.status.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown PaymentStatus: " + value));
    }

    @Override
    public String toString() {
        return status;
    }
}
