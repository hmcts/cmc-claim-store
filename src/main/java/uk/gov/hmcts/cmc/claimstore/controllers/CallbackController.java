package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CallbackHandlerFactory;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackVersion;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Optional;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/cases/callbacks",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
)
public class CallbackController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CallbackHandlerFactory callbackHandlerFactory;

    @Autowired
    public CallbackController(CallbackHandlerFactory callbackHandlerFactory) {
        this.callbackHandlerFactory = callbackHandlerFactory;
    }

    @PostMapping(path = {"/{callback-type}", "{version}/{callback-type}"})
    @ApiOperation("Handles all callbacks from CCD")
    @LogExecutionTime
    public CallbackResponse callback(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @PathVariable("callback-type") String callbackType,
        @NotNull @RequestBody CallbackRequest callback,
        @PathVariable("version") Optional<String> version
    ) {
        logger.info("Received callback from CCD, eventId: {}", callback.getEventId());
        CallbackParams callbackParams = CallbackParams.builder()
            .request(callback)
            .type(CallbackType.fromValue(callbackType))
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, authorisation))
            .version(version.map(String::toUpperCase).map(CallbackVersion::valueOf).orElse(null))
            .build();
        return callbackHandlerFactory
            .dispatch(callbackParams);
    }
}
