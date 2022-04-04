package uk.gov.hmcts.cmc.claimstore.requests.courtfinder;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Court;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.name.SearchCourtByNameResponse;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode.SearchCourtByPostcodeResponse;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.slug.SearchCourtBySlugResponse;

import java.util.List;

@FeignClient(name = "court-finder-api", primary = false, url = "${courtfinder.api.url}")
public interface CourtFinderApi {

    String SEARCH_POSTCODE_URL = "/search/results?postcode={postcode}&serviceArea=money-claims";
    String SEARCH_NAME_URL = "/courts?q={name}";
    String COURT_DETAILS_URL = "/courts/{slug}";

    @GetMapping(value = SEARCH_POSTCODE_URL)
    SearchCourtByPostcodeResponse findMoneyClaimCourtByPostcode(@PathVariable("postcode") String postcode);

    @GetMapping(value = COURT_DETAILS_URL)
    SearchCourtBySlugResponse getCourtDetailsFromNameSlug(@PathVariable("slug") String courtNameSlug);

    @GetMapping(value = SEARCH_NAME_URL)
    List<SearchCourtByNameResponse> findMoneyClaimCourtByName(@PathVariable("name") String name);

}
