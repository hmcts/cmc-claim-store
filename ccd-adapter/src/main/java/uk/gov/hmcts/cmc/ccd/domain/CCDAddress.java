package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDAddress {

    @JsonProperty("AddressLine1")
    private String addressLine1;
    @JsonProperty("AddressLine2")
    private String addressLine2;
    @JsonProperty("AddressLine3")
    private String addressLine3;
    @JsonProperty("PostTown")
    private String postTown;
    @JsonProperty("PostCode")
    private String postCode;
    @JsonProperty("Country")
    private String country;
    @JsonProperty("County")
    private String county;
}
