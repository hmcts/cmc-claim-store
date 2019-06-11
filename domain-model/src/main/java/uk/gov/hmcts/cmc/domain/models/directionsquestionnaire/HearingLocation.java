package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Address;

import javax.validation.constraints.NotNull;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class HearingLocation {

    @NotNull
    private final String courtName;
    private final Address courtAddress;
    @NotNull
    private final CourtLocation locationOption;
    private final String exceptionalCircumstancesReason;

    @Builder
    public HearingLocation(
        String courtName,
        Address courtAddress,
        CourtLocation locationOption,
        String exceptionalCircumstancesReason
    ) {
        this.courtName = courtName;
        this.courtAddress = courtAddress;
        this.locationOption = locationOption;
        this.exceptionalCircumstancesReason = exceptionalCircumstancesReason;
    }

    public Optional<Address> getCourtAddress() {
        return Optional.ofNullable(courtAddress);
    }

    public Optional<String> getExceptionalCircumstancesReason() {
        return Optional.ofNullable(exceptionalCircumstancesReason);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
