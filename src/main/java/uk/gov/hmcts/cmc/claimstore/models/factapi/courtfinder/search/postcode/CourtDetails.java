package uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class CourtDetails {

    private List<String> areasOfLawSpoe;

    private double distance;

    private String name;

    private String slug;
}
