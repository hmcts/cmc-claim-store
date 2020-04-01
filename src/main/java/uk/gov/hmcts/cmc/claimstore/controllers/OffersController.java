package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.OffersService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.converters.MadeByEnumConverter;

import javax.validation.Valid;

import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.UUID_PATTERN;

@Api
@RestController
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class OffersController {

    private final ClaimService claimService;
    private final OffersService offersService;

    @Autowired
    public OffersController(
        ClaimService claimService,
        OffersService offersService
    ) {
        this.claimService = claimService;
        this.offersService = offersService;
    }

    @InitBinder
    public void initWebDataBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(MadeBy.class, new MadeByEnumConverter());
    }

    @PostMapping(value = "/{externalId:" + UUID_PATTERN + "}/offers/{party}",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Makes an offer as a party")
    public Claim makeOffer(
        @PathVariable("externalId") String externalId,
        @PathVariable("party") MadeBy party,
        @RequestBody @Valid Offer offer,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        return offersService.makeOffer(claim, offer, party, authorisation);
    }

    @PostMapping(value = "/{externalId:" + UUID_PATTERN + "}/offers/{party}/accept",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Accepts an offer as a party")
    public Claim accept(
        @PathVariable("externalId") String externalId,
        @PathVariable("party") MadeBy party,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        return offersService.accept(claim, party, authorisation);
    }

    @PostMapping(value = "/{externalId:" + UUID_PATTERN + "}/offers/{party}/reject",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Rejects an offer as a party")
    public Claim reject(
        @PathVariable("externalId") String externalId,
        @PathVariable("party") MadeBy party,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        return offersService.reject(claim, party, authorisation);
    }

    @PostMapping(value = "/{externalId:" + UUID_PATTERN + "}/offers/{party}/countersign",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Counter sign an offer as a party")
    public Claim countersign(
        @PathVariable("externalId") String externalId,
        @PathVariable("party") MadeBy party,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        return offersService.countersign(claim, party, authorisation);
    }
}
