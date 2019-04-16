package uk.gov.hmcts.cmc.claimstore.courtfinder.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class CourtDetails {
    @Getter private String name;
    @Getter private String slug;
    @Getter private List<Facilities> facilities;
}
