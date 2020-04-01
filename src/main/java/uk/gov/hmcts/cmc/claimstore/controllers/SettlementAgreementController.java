package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.SettlementAgreementService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.UUID_PATTERN;

@Api
@RestController
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class SettlementAgreementController {

    private final ClaimService claimService;
    private final SettlementAgreementService settlementAgreementService;

    @Autowired
    public SettlementAgreementController(
        ClaimService claimService,
        SettlementAgreementService settlementAgreementService) {
        this.claimService = claimService;
        this.settlementAgreementService = settlementAgreementService;
    }

    @PostMapping(value = "/{externalId:" + UUID_PATTERN + "}/settlement-agreement/reject",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Rejects a settlement agreement as a defendant")
    public Claim reject(
        @PathVariable("externalId") String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        return settlementAgreementService.reject(claim, authorisation);
    }

    @PostMapping(value = "{externalId:" + UUID_PATTERN + "}/settlement-agreement/countersign",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Countersigns a settlement agreement as a defendant")
    public Claim counterSign(
        @PathVariable("externalId") String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        return settlementAgreementService.countersign(claim, authorisation);
    }
}
