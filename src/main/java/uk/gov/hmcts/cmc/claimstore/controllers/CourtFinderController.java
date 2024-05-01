package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Court;
import uk.gov.hmcts.cmc.claimstore.services.courtfinder.CourtFinderService;

import java.util.List;

@Tag(name = "Court Finder Controller")
@RestController
@RequestMapping(
    path = "/court-finder",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class CourtFinderController {

    private final CourtFinderService courtFinderService;

    @Autowired
    public CourtFinderController(CourtFinderService courtFinderService) {
        this.courtFinderService = courtFinderService;
    }

    @GetMapping(value = "/search-postcode/{postcode}")
    public List<Court> searchByPostcode(
        @NotEmpty @NotNull @PathVariable("postcode") String postcode) {
        return courtFinderService.getCourtDetailsListFromPostcode(postcode);
    }

    @GetMapping(value = "/court-details/{court-slug}")
    public Court getCourtDetails(@NotEmpty @NotNull @PathVariable("court-slug") String courtNameSlug) {
        return courtFinderService.getCourtDetailsFromSlug(courtNameSlug);
    }

    @GetMapping(value = "/search-name/{name}")
    public List<Court> searchByName(
        @NotEmpty @NotNull @PathVariable("name") String name) {
        return courtFinderService.getCourtsByName(name);
    }

}
