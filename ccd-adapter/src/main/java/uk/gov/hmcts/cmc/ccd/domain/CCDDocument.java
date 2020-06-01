package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CCDDocument {

    @JsonProperty("document_url")
    private String documentUrl;

    @JsonProperty("document_binary_url")
    private String documentBinaryUrl;

    @JsonProperty("document_filename")
    private String documentFileName;

    @JsonCreator
    public CCDDocument(String documentUrl, String documentBinaryUrl, String documentFileName) {
        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFileName = documentFileName;
    }
}
