package uk.gov.hmcts.cmc.domain.models;

import org.hibernate.validator.constraints.NotBlank;

import java.util.Objects;
import javax.validation.constraints.NotNull;

public class PaymentState {
    @NotBlank
    private final String status;

    @NotNull
    private final boolean finished;

    public PaymentState(String status, boolean finished) {
        this.status = status;
        this.finished = finished;
    }

    public String getStatus() {
        return status;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PaymentState that = (PaymentState) other;
        return finished == that.finished
            && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, finished);
    }
}
