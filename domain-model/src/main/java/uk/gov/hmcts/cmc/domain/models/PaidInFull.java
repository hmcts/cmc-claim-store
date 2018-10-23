package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class PaidInFull {

    @NotNull
    @DateNotInTheFuture
    private final LocalDate moneyReceivedOn;

    @Builder
    @JsonCreator
    public PaidInFull(LocalDate moneyReceivedOn) {
        this.moneyReceivedOn = moneyReceivedOn;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
