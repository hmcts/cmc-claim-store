package uk.gov.hmcts.cmc.claimstore.courtfinder;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;

import java.util.List;

@FeignClient(name = "court-finder-api", primary = false, url = "${courtfinder.api.url}")
public interface CourtFinderApi {

    String SEARCH_POSTCODE_URL = "/search/results?postcode={postcode}&spoe=nearest&aol=Money%20Claims";
    String SEARCH_NAME_URL = "/search/results?q={name}";
    String COURT_DETAILS_URL = "/courts/{slug}";

    @GetMapping(value = SEARCH_POSTCODE_URL)
    CourtFinderResponse findMoneyClaimCourtByPostcode(@PathVariable("postcode") String postcode);

    @GetMapping(value = COURT_DETAILS_URL)
    CourtDetails getCourtDetailsFromNameSlug(@PathVariable("slug") String courtNameSlug);

    @GetMapping(value = SEARCH_NAME_URL)
    List<Court> findMoneyClaimCourtByName(@PathVariable("name") String name);

}
