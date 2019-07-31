package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Optional;
import javax.validation.constraints.Size;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class DirectionsQuestionnaire {

    private final RequireSupport requireSupport;
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

    public Optional<RequireSupport> getRequireSupport() {
        return Optional.ofNullable(requireSupport);
    }

    public Optional<HearingLocation> getHearingLocation() {
        return Optional.ofNullable(hearingLocation);
    }

    public Optional<Witness> getWitness() {
        return Optional.ofNullable(witness);
    }

    public List<UnavailableDate> getUnavailableDates() {
        return Optional.ofNullable(unavailableDates).orElse(emptyList());
    }

    public Optional<ExpertRequest> getExpertRequest() {
        return Optional.ofNullable(expertRequest);
    }

    public List<ExpertReport> getExpertReports() {
        return Optional.ofNullable(expertReports).orElse(emptyList());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
