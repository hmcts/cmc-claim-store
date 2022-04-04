package uk.gov.hmcts.cmc.claimstore.models.courtfinder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class AreaOfLaw {
    @JsonProperty("display_url")
    String displayUrl;

    @JsonProperty("display_name")
    String displayName;

    @JsonProperty("external_link")
    String externalLink;

    @JsonProperty("external_link_desc")
    String externalDescription;

    String name;
}
