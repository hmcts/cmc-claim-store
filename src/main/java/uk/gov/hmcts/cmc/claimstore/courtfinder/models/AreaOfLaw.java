package uk.gov.hmcts.cmc.claimstore.courtfinder.models;

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
public class AreaOfLaw {
    String name;
    @JsonProperty("external_link")
    String externalLink;
    @JsonProperty("display_url")
    String displayUrl;
    @JsonProperty("external_link_desc")
    String externalDescription;
}
