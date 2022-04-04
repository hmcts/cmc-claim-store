package uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class SearchCourtByPostcodeResponse {

    private List<CourtDetails> courts;

    private String name;

    private String onlineText;

    private String onlineUrl;

    private String slug;

}
