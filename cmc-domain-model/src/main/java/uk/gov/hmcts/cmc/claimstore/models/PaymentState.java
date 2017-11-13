package uk.gov.hmcts.cmc.claimstore.models;

import org.hibernate.validator.constraints.NotBlank;

import java.util.Objects;
import javax.validation.constraints.NotNull;

public class PaymentState {
    @NotBlank
    private final String status;

    @NotNull
    private final boolean finished;

    public PaymentState(final String status, final boolean finished) {
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
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final PaymentState that = (PaymentState) other;
        return finished == that.finished
            && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, finished);
    }
}
