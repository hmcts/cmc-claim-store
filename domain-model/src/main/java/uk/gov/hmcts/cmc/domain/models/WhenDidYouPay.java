package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;
import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.Size;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public class WhenDidYouPay {

        @JsonUnwrapped
        @DateNotInTheFuture
        private final LocalDate paidDate;

        @NotBlank
        @Size(max = 99000)
        private final String explanation;

        public WhenDidYouPay(final LocalDate paidDate, String explanation) {
            this.paidDate = paidDate;
            this.explanation = explanation;
        }

        public LocalDate getWhenDidYouPay() {
            return paidDate;
        }

        public String getHowDidYouPay() {
            return explanation;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            final WhenDidYouPay that = (WhenDidYouPay) other;
            return Objects.equals(paidDate, that.paidDate)
                && Objects.equals(explanation, that.explanation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(paidDate, explanation);
        }

        @Override
        public String toString() {
            return String.format(
                "WhenDidYouPay{paidDate=%s, howDidYouPay='%s'}",
                paidDate, explanation
            );
        }
    }
