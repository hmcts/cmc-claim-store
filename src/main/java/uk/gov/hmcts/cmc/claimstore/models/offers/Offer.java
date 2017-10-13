package uk.gov.hmcts.cmc.claimstore.models.offers;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.claimstore.constraints.FutureDate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

public class Offer {

    static final int CONTENT_LENGTH_LIMIT = 99000;

    @NotBlank
    @Size(max = CONTENT_LENGTH_LIMIT, message = "may not be longer than {max} characters")
    String content;

    @NotNull
    @FutureDate
    LocalDate completionDate;

    public Offer(String content, LocalDate completionDate) {
        this.content = content;
        this.completionDate = completionDate;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    @Override
    public boolean equals(Object input) {
        if (this == input) {
            return true;
        }
        if (input == null || getClass() != input.getClass()) {
            return false;
        }

        Offer other = (Offer) input;
        return Objects.equals(content, other.content) &&
            Objects.equals(completionDate, other.completionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, completionDate);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
