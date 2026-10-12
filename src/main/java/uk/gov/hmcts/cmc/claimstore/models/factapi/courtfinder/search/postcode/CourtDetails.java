package uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourtDetails {

    private UUID courtId;

    private double distance;

    @JsonProperty("courtName")
    private String name;

    @JsonProperty("courtSlug")
    private String slug;
}
