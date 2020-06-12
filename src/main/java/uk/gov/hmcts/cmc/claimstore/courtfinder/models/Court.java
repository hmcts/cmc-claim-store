package uk.gov.hmcts.cmc.claimstore.courtfinder.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class Court {
    private String name;
    private String slug;
    private Address address;
    @JsonProperty("areas_of_law")
    private List<AreaOfLaw> areasOfLaw;
}
