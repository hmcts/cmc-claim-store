package uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Court {
    private String name;

    private String slug;

    private double distance;

    private List<AreasOfLaw> areasOfLawSpoe;
}
