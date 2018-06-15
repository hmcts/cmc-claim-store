package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDDocument {

    @JsonProperty("document_url")
    private String documentUrl;
}
