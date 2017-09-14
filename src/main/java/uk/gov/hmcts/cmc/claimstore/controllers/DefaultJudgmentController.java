package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.models.DefaultJudgment;
import uk.gov.hmcts.cmc.claimstore.services.DefaultJudgmentService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;

import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/default-judgment",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DefaultJudgmentController {

    private DefaultJudgmentService defaultJudgmentService;
    private UserService userService;

    @Autowired
    public DefaultJudgmentController(
        final DefaultJudgmentService defaultJudgmentService, final UserService userService) {
        this.defaultJudgmentService = defaultJudgmentService;
        this.userService = userService;
    }

    @GetMapping("/{defaultJudgmentId:\\d+}")
    @ApiOperation("Fetch default judgment by id")
    public DefaultJudgment getBySubmitterId(@PathVariable("defaultJudgmentId") final Long defaultJudgmentId) {
        return defaultJudgmentService.getByClaimId(defaultJudgmentId);
    }

    @PostMapping("/{claimId:\\d+}")
    @ApiOperation("Save default judgment")
    public DefaultJudgment save(
        @PathVariable("claimId") final Long claimId,
        @NotNull @RequestBody final String defaultJudgment,
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation
    ) {
        final long submitterId = userService.getUserDetails(authorisation).getId();
        return defaultJudgmentService.save(submitterId, defaultJudgment, claimId);
    }
}
