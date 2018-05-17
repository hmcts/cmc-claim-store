package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
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

    private final ClaimService claimService;

    @Autowired
    public CallbackController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping(path = "/{callback-type}")
    @ApiOperation("Request more time for response arrived on paper")
    public CallbackResponse callback(
        @PathVariable("callback-type") String callbackType,
        @NotNull @RequestBody CallbackRequest callback
    ) {

        if (callback.getEventId().equals(CaseEvent.MORE_TIME_REQUESTED_PAPER.getValue())) {
            switch (callbackType) {
                case "about-to-start":
                    return claimService.requestMoreTimeOnPaper(callback, true);
                case "about-to-submit":
                    return claimService.requestMoreTimeOnPaper(callback, false);
                case "submitted":
                    return claimService.requestMoreTimeOnPaperSubmitted(callback);
                default:
                    throw new IllegalArgumentException("Unknown callback type: " + callbackType);
            }
        } else {
            throw new IllegalArgumentException("Unknown event: " + callback.getEventId());
        }
    }
}
