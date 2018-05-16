package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.reform.ccd.client.model.AboutToSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseCallback;

import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/cases/callbacks",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class CallbackController {

    private final ClaimService claimService;

    @Autowired
    public CallbackController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping(path = "/more-time-requested-paper")
    @ApiOperation("Request more time for response arrived on paper")
    public AboutToSubmitCallbackResponse requestMoreTimeForResponseOnPaper(@NotNull @RequestBody CaseCallback callback) {
        AboutToSubmitCallbackResponse callbackResponse = claimService.requestMoreTimeForResponseOnPaper(callback);
        return callbackResponse;
    }
}
