package uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourtFinderResponse {
    private String slug;

    private String name;

    private String onlineText;

    private String onlineUrl;

    private List<Court> courts;
}
