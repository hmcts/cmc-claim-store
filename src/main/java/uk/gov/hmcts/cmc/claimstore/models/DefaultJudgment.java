package uk.gov.hmcts.cmc.claimstore.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.time.LocalDateTime;
import java.util.Objects;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class DefaultJudgment {

    private final Long id;
    private final Long claimId;
    private final Long claimantId;
    private final String externalId;
    private final String data;
    private final LocalDateTime createdAt;

    public DefaultJudgment(
        final Long id,
        final Long claimId,
        final Long claimantId,
        final String externalId,
        final String data,
        final LocalDateTime createdAt) {
        this.id = id;
        this.claimId = claimId;
        this.claimantId = claimantId;
        this.externalId = externalId;
        this.data = data;
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DefaultJudgment otherCcj = (DefaultJudgment) other;
        return Objects.equals(id, otherCcj.id)
            && Objects.equals(claimId, otherCcj.claimId)
            && Objects.equals(claimantId, otherCcj.claimantId)
            && Objects.equals(externalId, otherCcj.externalId)
            && Objects.equals(createdAt, otherCcj.createdAt)
            && Objects.equals(data, otherCcj.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id, claimId, claimantId, externalId, data, createdAt
        );
    }

    public static class Builder {
        private Long id;
        private Long claimId;
        private Long claimantId;
        private String externalId;
        private String data;
        private LocalDateTime createdAt;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setClaimId(Long claimId) {
            this.claimId = claimId;
            return this;
        }

        public Builder setClaimantId(Long claimantId) {
            this.claimantId = claimantId;
            return this;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DefaultJudgment build() {
            return new DefaultJudgment(id, claimId, claimantId, externalId, data, createdAt);
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
