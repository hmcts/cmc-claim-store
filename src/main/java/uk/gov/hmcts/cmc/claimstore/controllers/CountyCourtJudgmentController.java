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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.CountyCourtJudgmentService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;

import java.util.Map;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "claims/county-court-judgment",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CountyCourtJudgmentController {

    private CountyCourtJudgmentService countyCourtJudgmentService;
    private UserService userService;

    @Autowired
    public CountyCourtJudgmentController(
        final CountyCourtJudgmentService countyCourtJudgmentService, final UserService userService) {
        this.countyCourtJudgmentService = countyCourtJudgmentService;
        this.userService = userService;
    }

    @PostMapping("/{claimId:\\d+}")
    @ApiOperation("Save County Court Judgment")
    public Claim save(
        @PathVariable("claimId") final Long claimId,
        @NotNull @RequestBody final Map<String, Object> countyCourtJudgment,
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation
    ) {
        final long submitterId = userService.getUserDetails(authorisation).getId();
        return countyCourtJudgmentService.save(submitterId, countyCourtJudgment, claimId);
    }
}
