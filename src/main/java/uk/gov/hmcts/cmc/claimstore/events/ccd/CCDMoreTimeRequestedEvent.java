package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class CCDMoreTimeRequestedEvent {

    private final String authorization;
    private final String externalId;
    private final LocalDate newDeadline;

    public CCDMoreTimeRequestedEvent(String authorization, String externalId, LocalDate newDeadline) {
        this.authorization = authorization;
        this.externalId = externalId;
        this.newDeadline = newDeadline;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
