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
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
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

    public static final String ABOUT_TO_START_CALLBACK = "about-to-start";
    public static final String ABOUT_TO_SUBMIT_CALLBACK = "about-to-submit";
    public static final String SUBMITTED_CALLBACK = "submitted";

    private final ClaimService claimService;

    @Autowired
    public CallbackController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping(path = "/{callback-type}")
    @ApiOperation("Handles all callbacks from CCD")
    public CallbackResponse callback(
        @PathVariable("callback-type") String callbackType,
        @NotNull @RequestBody CallbackRequest callback
    ) {

        if (callback.getEventId().equals(CaseEvent.MORE_TIME_REQUESTED_PAPER.getValue())) {
            switch (callbackType) {
                case ABOUT_TO_START_CALLBACK:
                    return claimService.requestMoreTimeOnPaper(callback, true);
                case ABOUT_TO_SUBMIT_CALLBACK:
                    return claimService.requestMoreTimeOnPaper(callback, false);
                case SUBMITTED_CALLBACK:
                    return claimService.requestMoreTimeOnPaperSubmitted(callback);
                default:
                    throw new BadRequestException("Unknown callback type: " + callbackType);
            }
        } else {
            throw new BadRequestException("Unknown event: " + callback.getEventId());
        }
    }
}
