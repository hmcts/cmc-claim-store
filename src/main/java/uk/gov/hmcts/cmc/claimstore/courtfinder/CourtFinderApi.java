package uk.gov.hmcts.cmc.claimstore.courtfinder;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;

import java.util.List;

@FeignClient(name = "court-finder-api", url = "${courtfinder.api.url}")
public interface CourtFinderApi {

    String SEARCH_POSTCODE_URL = "/search/results.json?postcode={postcode}&spoe=nearest&aol=Money%20Claims";
    String SEARCH_NAME_URL = "/search/results.json?q={name}";
    String COURT_DETAILS_URL = "/courts/{slug}.json";

    @RequestMapping(method = RequestMethod.GET, value = SEARCH_POSTCODE_URL)
    List<Court> findMoneyClaimCourtByPostcode(@PathVariable("postcode") String postcode);

    @RequestMapping(method = RequestMethod.GET, value = COURT_DETAILS_URL)
    CourtDetails getCourtDetailsFromNameSlug(@PathVariable("slug") String courtNameSlug);

    @RequestMapping(method = RequestMethod.GET, value = SEARCH_NAME_URL)
    List<Court> findMoneyClaimCourtByName(@PathVariable("name") String name);

}
