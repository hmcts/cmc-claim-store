package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class DirectionsQuestionnaire {

    private final RequireSupport requireSupport;

    @NotNull
    private final HearingLocation hearingLocation;
    private final Witness witness;
    @Size(min = 1)
    private final List<ExpertReport> expertReports;
    private final List<UnavailableDate> unavailableDates;
    private final ExpertRequest expertRequest;

    @Builder
    public DirectionsQuestionnaire(
        RequireSupport requireSupport,
        HearingLocation hearingLocation,
        Witness witness,
        List<ExpertReport> expertReports,
        List<UnavailableDate> unavailableDates,
        ExpertRequest expertRequest
    ) {
        this.requireSupport = requireSupport;
        this.hearingLocation = hearingLocation;
        this.witness = witness;
        this.expertReports = expertReports;
        this.unavailableDates = unavailableDates;
        this.expertRequest = expertRequest;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
