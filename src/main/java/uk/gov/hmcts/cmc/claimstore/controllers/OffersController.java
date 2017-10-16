package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.offers.Offer;
import uk.gov.hmcts.cmc.claimstore.services.AuthorisationService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.OffersService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;

import javax.validation.Valid;

@Api
@RestController
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OffersController {

    private ClaimService claimService;
    private UserService userService;
    private AuthorisationService authorisationService;
    private OffersService offersService;

    @Autowired
    public OffersController(
        ClaimService claimService,
        UserService userService,
        AuthorisationService authorisationService,
        OffersService offersService) {
        this.claimService = claimService;
        this.userService = userService;
        this.authorisationService = authorisationService;
        this.offersService = offersService;
    }

    @PostMapping(value = "/{claimId:\\d+}/offers/{party}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Save County Court Judgment")
    public Claim makeOffer(
        @PathVariable("claimId") Long claimId,
        @PathVariable("party") MadeBy party,
        @RequestBody @Valid Offer offer,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        Claim claim = claimService.getClaimById(claimId);
        assertActionIsPermittedFor(claim, authorisation);
        return offersService.makeOffer(claim, offer, party);
    }

    private void assertActionIsPermittedFor(Claim claim, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        authorisationService.assertIsPartyOnClaim(claim, userDetails.getId());
    }

}
