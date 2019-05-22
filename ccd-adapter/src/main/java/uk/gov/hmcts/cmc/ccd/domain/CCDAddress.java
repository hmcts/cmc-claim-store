package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (addressLine1 != null) {
            stringBuilder.append(addressLine1);
            stringBuilder.append("\n");
        }
        if (addressLine2 != null) {
            stringBuilder.append(addressLine2);
            stringBuilder.append("\n");
        }
        if (addressLine3 != null) {
            stringBuilder.append(addressLine3);
            stringBuilder.append("\n");
        }
        if (postCode != null) {
            stringBuilder.append(postCode);
            stringBuilder.append("\n");
        }
        if (postTown != null) {
            stringBuilder.append(postTown);
        }
        return stringBuilder.toString();
    }
}
