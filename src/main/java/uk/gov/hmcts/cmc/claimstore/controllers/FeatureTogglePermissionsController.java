package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.UserAuthorizedRolesService;
import uk.gov.hmcts.cmc.domain.models.AuthorizedRole;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.UserRole;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/users",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class FeatureTogglePermissionsController {

    private final UserAuthorizedRolesService userAuthorizedRolesService;

    public FeatureTogglePermissionsController(UserAuthorizedRolesService userAuthorizedRolesService) {
        this.userAuthorizedRolesService = userAuthorizedRolesService;
    }

    @GetMapping("/roles")
    @ApiOperation("Fetch user roles")
    public String getBySubmitterId(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        return userAuthorizedRolesService.authorizedRole(authorisation);
    }

    @PostMapping(value = "/roles/assign", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("Creates a new AuthorizedRole")
    public AuthorizedRole save(
        @Valid @NotNull @RequestBody UserRole userRole,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return userAuthorizedRolesService.saveRole(userRole, authorisation);
    }
}
