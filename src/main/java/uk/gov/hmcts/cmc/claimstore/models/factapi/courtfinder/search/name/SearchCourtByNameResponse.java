package uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.name;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchCourtByNameResponse {

    private Boolean displayed;

    private String name;

    private String slug;

    @JsonProperty("updated_at")
    private String updatedAt;
}
