package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ResponseRejection extends ClaimantResponse {

    private final Boolean freeMediation;

    @Size(max = 99000)
    private final String reason;

    @Builder
    @JsonCreator
    public ResponseRejection(BigDecimal amountPaid, boolean freeMediation, String reason) {
        super(amountPaid);
        this.freeMediation = freeMediation;
        this.reason = reason;
    }

    public Optional<Boolean> getFreeMediation() {
        return Optional.ofNullable(freeMediation);
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
