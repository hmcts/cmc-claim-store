package uk.gov.hmcts.cmc.claimstore.models.offers;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.claimstore.constraints.FutureDate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

public class Offer {

    static final int CONTENT_LENGTH_LIMIT = 99000;

    @NotBlank
    @Size(max = CONTENT_LENGTH_LIMIT, message = "may not be longer than {max} characters")
    private String content;

    @NotNull
    @FutureDate
    private LocalDate completionDate;

    private Response response = Response.PENDING;

    private Offer counterOffer;

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

    public Response getResponse() {
        return response;
    }

    public Optional<Offer> getCounterOffer() {
        return Optional.ofNullable(counterOffer);
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
            Objects.equals(completionDate, other.completionDate) &&
            Objects.equals(response, other.response) &&
            Objects.equals(counterOffer, other.counterOffer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, completionDate, response, counterOffer);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
