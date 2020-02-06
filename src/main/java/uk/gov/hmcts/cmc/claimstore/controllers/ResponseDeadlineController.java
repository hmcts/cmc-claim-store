package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;

import java.time.LocalDate;

@Api
@RestController
@RequestMapping(
    path = "/deadline",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class ResponseDeadlineController {
    private final ResponseDeadlineCalculator calculator;

    public ResponseDeadlineController(ResponseDeadlineCalculator calculator) {
        this.calculator = calculator;
    }

    @GetMapping("/{issueDate}")
    public LocalDate postponedDeadline(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable LocalDate issueDate
    ) {
        return calculator.calculatePostponedResponseDeadline(issueDate);
    }
}
