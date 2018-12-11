package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(value = {"document_filename", "document_binary_url"})
public class CCDDocument {

    @JsonProperty("document_url")
    private String documentUrl;

    @JsonCreator
    public CCDDocument(String documentUrl) {
        this.documentUrl = documentUrl;
    }
}
