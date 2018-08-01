package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.FeatureTogglesService;
import uk.gov.hmcts.cmc.domain.models.AuthorizedRole;
import uk.gov.hmcts.cmc.domain.models.UserRole;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/users",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class FeatureTogglePermissionsController {

    private final FeatureTogglesService featureTogglesService;

    public FeatureTogglePermissionsController(FeatureTogglesService featureTogglesService) {
        this.featureTogglesService = featureTogglesService;
    }

    @GetMapping("/roles")
    @ApiOperation("Fetch user roles")
    public String getBySubmitterId(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        return featureTogglesService.authorizedRole(authorisation);
    }

    @PostMapping(value = "/roles/assign", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("Creates a new AuthorizedRole")
    public AuthorizedRole save(
        @Valid @NotNull @RequestBody UserRole userRole,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return featureTogglesService.saveRole(userRole, authorisation);
    }
}
