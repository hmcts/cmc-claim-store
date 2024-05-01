package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.CountyCourtJudgmentService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;

import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.UUID_PATTERN;

@Tag(name = "County Court Judgment Controller")
@RestController
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class CountyCourtJudgmentController {

    private final CountyCourtJudgmentService countyCourtJudgmentService;

    @Autowired
    public CountyCourtJudgmentController(CountyCourtJudgmentService countyCourtJudgmentService) {
        this.countyCourtJudgmentService = countyCourtJudgmentService;
    }

    @PostMapping("/{externalId:" + UUID_PATTERN + "}/county-court-judgment")
    @Operation(summary = "Save County Court Judgment")
    public Claim save(
        @PathVariable("externalId") String externalId,
        @NotNull @RequestBody @Valid CountyCourtJudgment countyCourtJudgment,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return countyCourtJudgmentService.save(countyCourtJudgment, externalId, authorisation);
    }

    @PostMapping("/{externalId:" + UUID_PATTERN + "}/re-determination")
    @Operation(summary = "ReDetermination Request to Judge")
    public Claim reDetermination(
        @PathVariable("externalId") String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @NotNull @RequestBody @Valid ReDetermination reDetermination
    ) {
        return countyCourtJudgmentService.reDetermination(reDetermination, externalId, authorisation);
    }
}
