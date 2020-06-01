package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/court-finder",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class CourtFinderController {

    private final CourtFinderApi courtFinderApi;
    private static final String MONEY_CLAIM_AOL = "Money claims";

    @Autowired
    public CourtFinderController(CourtFinderApi courtFinderApi) {
        this.courtFinderApi = courtFinderApi;
    }

    @GetMapping(value = "/search-postcode/{postcode}")
    public List<Court> searchByPostcode(
        @NotEmpty @NotNull @PathVariable("postcode") String postcode) {
        return courtFinderApi.findMoneyClaimCourtByPostcode(postcode);
    }

    @GetMapping(value = "/court-details/{court-slug}")
    public CourtDetails getCourtDetails(@NotEmpty @NotNull @PathVariable("court-slug") String courtNameSlug) {
        return courtFinderApi.getCourtDetailsFromNameSlug(courtNameSlug);
    }

    @GetMapping(value = "/search-name/{name}")
    public List<Court> searchByName(
        @NotEmpty @NotNull @PathVariable("name") String name) {
        return courtFinderApi.findMoneyClaimCourtByName(name)
            .stream()
            .filter(
                c -> c.getAreasOfLaw()
                    .stream()
                    .anyMatch(a -> a.getName().equalsIgnoreCase(MONEY_CLAIM_AOL))
            ).collect(Collectors.toList());
    }

}
