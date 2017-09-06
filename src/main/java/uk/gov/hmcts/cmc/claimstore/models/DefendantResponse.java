package uk.gov.hmcts.cmc.claimstore.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class DefendantResponse {

    private final Long id;
    private final Long claimId;
    private final Long defendantId;
    private final String defendantEmail;
    private final ResponseData response;
    private final LocalDateTime respondedAt;

    public DefendantResponse(
        final Long id,
        final Long claimId,
        final Long defendantId,
        final String defendantEmail,
        final ResponseData response,
        final LocalDateTime respondedAt
    ) {

        this.id = id;
        this.claimId = claimId;
        this.defendantId = defendantId;
        this.defendantEmail = defendantEmail;
        this.response = response;
        this.respondedAt = respondedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getClaimId() {
        return claimId;
    }

    public Long getDefendantId() {
        return defendantId;
    }

    public String getDefendantEmail() {
        return defendantEmail;
    }

    public ResponseData getResponse() {
        return response;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final DefendantResponse that = (DefendantResponse) other;
        return Objects.equals(id, that.id)
            && Objects.equals(claimId, that.claimId)
            && Objects.equals(defendantId, that.defendantId)
            && Objects.equals(defendantEmail, that.defendantEmail)
            && Objects.equals(response, that.response)
            && Objects.equals(respondedAt, that.respondedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, claimId, defendantId, defendantEmail, response, respondedAt);
    }

    @Override
    public String toString() {
        return String.format(
            "DefendantResponse{id=%d, claimId=%d, defendantId=%d, defendantEmail=%s, response=%s, respondedAt=%s}",
            id, claimId, defendantId, defendantEmail, response, respondedAt
        );
    }
}
