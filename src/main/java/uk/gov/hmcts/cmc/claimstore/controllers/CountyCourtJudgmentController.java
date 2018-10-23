package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.CountyCourtJudgmentService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.Redetermination;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.UUID_PATTERN;

@Api
@RestController
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CountyCourtJudgmentController {

    private CountyCourtJudgmentService countyCourtJudgmentService;
    private UserService userService;

    @Autowired
    public CountyCourtJudgmentController(
        CountyCourtJudgmentService countyCourtJudgmentService, UserService userService) {
        this.countyCourtJudgmentService = countyCourtJudgmentService;
        this.userService = userService;
    }

    @PostMapping("/{externalId:" + UUID_PATTERN + "}/county-court-judgment")
    @ApiOperation("Save County Court Judgment")
    public Claim save(
        @PathVariable("externalId") String externalId,
        @NotNull @RequestBody @Valid CountyCourtJudgment countyCourtJudgment,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestParam(name = "issue", required = false) boolean issue
    ) {
        String submitterId = userService.getUserDetails(authorisation).getId();
        return countyCourtJudgmentService.save(submitterId, countyCourtJudgment, externalId, authorisation, issue);
    }

    @PostMapping("/{externalId:" + UUID_PATTERN + "}/redetermination")
    @ApiOperation("Redetermination Request to Judge")
    public Claim redetermination(
        @PathVariable("externalId") String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @NotNull @RequestBody @Valid Redetermination redetermination
    ) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        return countyCourtJudgmentService.redetermination(userDetails, redetermination, externalId, authorisation);
    }
}
