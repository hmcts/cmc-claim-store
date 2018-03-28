package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.InterestAmount;

import java.math.BigDecimal;
import java.time.LocalDate;

import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.UUID_PATTERN;

@Api
@RestController
@RequestMapping(
    path = "/interest",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class InterestRatesController {

    private final ClaimService claimService;

    @Autowired
    public InterestRatesController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping("/calculate")
    @ApiOperation("Calculates the interest amount accrued between provided dates")
    public InterestAmount calculateInterest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("from_date") LocalDate from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("to_date") LocalDate to,
        @RequestParam("rate") BigDecimal rate,
        @RequestParam("amount") BigDecimal amount
    ) {
        try {
            return new InterestAmount(TotalAmountCalculator.calculateInterest(amount, rate, from, to));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @GetMapping("/calculate/{externalId:" + UUID_PATTERN + "}")
    @ApiOperation("Calculates the interest for given external id")
    public InterestAmount calculateInterest(@PathVariable("externalId") String externalId,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {

        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);

        return new InterestAmount(TotalAmountCalculator.calculateInterestTillToday(claim));
    }
}
