package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.offers.Offer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OffersController {

    @PostMapping(value = "/{claimId:\\d+}/offers/{party}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Save County Court Judgment")
    public Claim makeOffer(
        @PathVariable("claimId") Long claimId,
        @PathVariable("party") MadeBy party,
        @RequestBody @Valid Offer offer,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return null;
    }

}
