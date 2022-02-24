package uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({"address_lines", "postcode", "town", "type"})
public class CourtAddress {
    @JsonProperty("type")
    private String type;
    @JsonProperty("address_lines")
    private List<String> addressLines;
    @JsonProperty("town")
    private String town;
    private String postcode;
}
