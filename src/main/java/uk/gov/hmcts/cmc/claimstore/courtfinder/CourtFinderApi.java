package uk.gov.hmcts.cmc.claimstore.courtfinder;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;

@FeignClient(name = "court-finder-api", primary = false, url = "${courtfinder.api.url}")
public interface CourtFinderApi {

    String SEARCH_POSTCODE_URL = "/search/results?postcode={postcode}&serviceArea=money-claims";
    String COURT_DETAILS_URL = "/courts/{slug}.json";

    @GetMapping(value = SEARCH_POSTCODE_URL)
    CourtFinderResponse findMoneyClaimCourtByPostcode(@PathVariable("postcode") String postcode);

    @GetMapping(value = COURT_DETAILS_URL)
    CourtDetails getCourtDetailsFromNameSlug(@PathVariable("slug") String courtNameSlug);
}
