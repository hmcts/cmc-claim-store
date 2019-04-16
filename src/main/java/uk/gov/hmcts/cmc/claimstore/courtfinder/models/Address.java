package uk.gov.hmcts.cmc.claimstore.courtfinder.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class Address {
    @JsonProperty("address_lines")
    @Getter private List<String> addressLines;
    @Getter private String postcode;
    @Getter private String town;
}
