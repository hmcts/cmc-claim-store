package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@EqualsAndHashCode
@Getter
public class ReviewOrder {

    public enum RequestedBy { CLAIMANT, DEFENDANT }

    @Size(max = 1000)
    private final String reason;
    @NotNull
    private final RequestedBy requestedBy;
    @NotNull
    private final LocalDateTime requestedAt;

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }
}
