package uk.gov.hmcts.cmc.claimstore.courtfinder.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class AreaOfLaw {
    String name;
    @JsonProperty("external_link")
    String externalLink;
    @JsonProperty("display_url")
    String displayUrl;
    @JsonProperty("external_link_desc")
    String externalDescription;
}
