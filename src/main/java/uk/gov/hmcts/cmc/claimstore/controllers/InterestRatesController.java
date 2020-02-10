package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.InterestAmount;

import java.math.BigDecimal;
import java.time.LocalDate;

@Api
@RestController
@RequestMapping(
    path = "/interest",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class InterestRatesController {

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
}
