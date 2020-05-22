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
import uk.gov.hmcts.cmc.domain.models.response.Response;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.UUID_PATTERN;

@Api
@RestController
@RequestMapping(
    path = "/responses",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class DefendantResponseController {
    private final DefendantResponseService defendantResponseService;

    @Autowired
    public DefendantResponseController(DefendantResponseService defendantResponseService) {
        this.defendantResponseService = defendantResponseService;
    }

    @PostMapping(
        value = "/claim/{externalId:" + UUID_PATTERN + "}/defendant/{defendantId}",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Creates a new defendant response")
    public Claim save(
        @Valid @NotNull @RequestBody Response response,
        @PathVariable("defendantId") String defendantId,
        @PathVariable("externalId") String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return defendantResponseService.save(externalId, defendantId, response, authorization);
    }
}
