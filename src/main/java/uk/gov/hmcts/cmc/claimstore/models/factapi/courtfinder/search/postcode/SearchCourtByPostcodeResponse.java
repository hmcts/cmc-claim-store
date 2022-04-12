package uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchCourtByPostcodeResponse {

    private List<CourtDetails> courts;

    private String name;

    private String onlineText;

    private String onlineUrl;

    private String slug;

}
