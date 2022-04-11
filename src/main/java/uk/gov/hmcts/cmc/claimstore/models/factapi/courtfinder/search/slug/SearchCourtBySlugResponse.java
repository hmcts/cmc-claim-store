package uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.slug;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Address;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.AreaOfLaw;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Facilities;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchCourtBySlugResponse {

    private List<Address> addresses;

    @JsonProperty("areas_of_law")
    private List<AreaOfLaw> areasOfLaw;

    private List<Facilities> facilities;

    private String name;

    private String slug;

}
