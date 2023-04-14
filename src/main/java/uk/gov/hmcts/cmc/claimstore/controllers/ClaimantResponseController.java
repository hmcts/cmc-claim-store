package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.ClaimantResponseService;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.UUID_PATTERN;

@Tag(name = "Claimant Response Controller")
@RestController
@RequestMapping(
    path = "/responses",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class ClaimantResponseController {

    private final ClaimantResponseService claimantResponseService;

    public ClaimantResponseController(ClaimantResponseService claimantResponseService) {
        this.claimantResponseService = claimantResponseService;
    }

    @PostMapping(value = "/{externalId:" + UUID_PATTERN + "}/claimant/{claimantId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creates a new claimant response")
    public void save(
        @Valid @NotNull @RequestBody ClaimantResponse response,
        @PathVariable("claimantId") String claimantId,
        @PathVariable("externalId") String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        claimantResponseService.save(externalId, claimantId, response, authorization);
    }
}
