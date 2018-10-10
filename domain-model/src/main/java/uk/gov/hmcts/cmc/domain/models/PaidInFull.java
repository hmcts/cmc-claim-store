package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;


@EqualsAndHashCode
public class PaidInFull {

    @NotNull
    @DateNotInTheFuture
    private final LocalDate moneyReceivedOn;

    public LocalDate getMoneyReceivedOn() {
        return moneyReceivedOn;
    }

    @Builder
    public PaidInFull(LocalDate moneyReceivedOn) {
        this.moneyReceivedOn = moneyReceivedOn;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
