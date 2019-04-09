package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.CallbackService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/cases/callbacks",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class CallbackController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ClaimService claimService;
    private final CallbackService callbackService;

    @Autowired
    public CallbackController(ClaimService claimService, CallbackService callbackService) {
        this.claimService = claimService;
        this.callbackService = callbackService;
    }

    @PostMapping(path = "/{callback-type}")
    @ApiOperation("Handles all callbacks from CCD")
    public CallbackResponse callback(
        @PathVariable("callback-type") String callbackType,
        @NotNull @RequestBody CallbackRequest callback
    ) {
        logger.info("Received callback from CCD, type: {}, eventId: {}", callbackType, callback.getEventId());
        return callbackService
            .getCallbackFor(callback.getEventId(), callbackType)
            .execute(claimService, callback);
    }
}
