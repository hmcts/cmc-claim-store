package uk.gov.hmcts.cmc.claimstore.courtfinder.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class Court {
    @Getter private String name;
    @Getter private String slug;
    @Getter private Address address;
}
