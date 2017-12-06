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
import uk.gov.hmcts.cmc.claimstore.services.DefendantResponseService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Response;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/responses",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DefendantResponseController {
    private final DefendantResponseService defendantResponseService;

    @Autowired
    public DefendantResponseController(final DefendantResponseService defendantResponseService) {
        this.defendantResponseService = defendantResponseService;
    }

    @PostMapping(
        value = "/claim/{claimId}/defendant/{defendantId:\\d+}",
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("Creates a new defendant response")
    public Claim save(
        @Valid @NotNull @RequestBody final Response response,
        @PathVariable("defendantId") final String defendantId,
        @PathVariable("claimId") final Long claimId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorization
    ) {
        return defendantResponseService.save(claimId, defendantId, response, authorization);
    }
}
