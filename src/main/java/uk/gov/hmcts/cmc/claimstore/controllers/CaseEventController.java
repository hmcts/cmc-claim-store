package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;

import java.util.List;


@Api
@RestController
@RequestMapping(
        path = "/case-events",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CaseEventController {

    private final ClaimService claimService;

    @Autowired
    public CaseEventController(
            ClaimService claimService
    ) {
        this.claimService = claimService;
    }

    @GetMapping(path = "/{claimID}/{userID}")
    @ApiOperation("Fetch case events for given reference number")
    public List<CaseEventDetail> getClaimEventsByID(
        @PathVariable("claimID") String claimID,
        @PathVariable("userID") String userID) {
//        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation)
        return claimService.getClaimEventsByID(claimID, userID);
    }

}
