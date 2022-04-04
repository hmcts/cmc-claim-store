package uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.slug;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Address;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.AreaOfLaw;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Facilities;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class SearchCourtBySlugResponse {

    private List<Address> addresses;

    @JsonProperty("areas_of_law")
    private List<AreaOfLaw> areasOfLaw;

    private List<Facilities> facilities;

    private String name;

    private String slug;

}
