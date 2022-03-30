package uk.gov.hmcts.cmc.claimstore.courtfinder;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;

import java.util.List;

@FeignClient(name = "court-finder-api", primary = false, url = "${courtfinder.api.legacy.url}")
public interface LegacyCourtFinderApi {

    String SEARCH_NAME_URL = "/search/results.json?q={name}";
    String COURT_DETAILS_URL = "/courts/{slug}.json";

    @GetMapping(value = COURT_DETAILS_URL)
    CourtDetails getCourtDetailsFromNameSlug(@PathVariable("slug") String courtNameSlug);

    @GetMapping(value = SEARCH_NAME_URL)
    List<Court> findMoneyClaimCourtByName(@PathVariable("name") String name);

}
