package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class DirectionsQuestionnaire {

    @Valid
    private final RequireSupport requireSupport;

    @Valid
    @NotNull
    private final HearingLocation hearingLocation;

    @Valid
    private final Witness witness;

    @Valid
    @Size(min = 1, max = 20)
    private final List<ExpertReport> expertReports;

    @Valid
    @Size(max = 280)
    private final List<UnavailableDate> unavailableDates;

    @Valid
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

    public Optional<RequireSupport> getRequireSupport() {
        return Optional.ofNullable(requireSupport);
    }

    public Optional<Witness> getWitness() {
        return Optional.ofNullable(witness);
    }

    public List<UnavailableDate> getUnavailableDates() {
        return unavailableDates == null ? emptyList() : unavailableDates;
    }

    public Optional<ExpertRequest> getExpertRequest() {
        return Optional.ofNullable(expertRequest);
    }

    public List<ExpertReport> getExpertReports() {
        return expertReports == null ? emptyList() : expertReports;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
