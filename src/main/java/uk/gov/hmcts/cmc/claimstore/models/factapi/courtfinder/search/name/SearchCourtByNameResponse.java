package uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.name;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class SearchCourtByNameResponse {

    private UUID id;

    private Boolean open;

    private String name;

    private String slug;

    private String lastUpdatedAt;
}
