package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.UserRolesService;
import uk.gov.hmcts.cmc.domain.models.UserRoleRequest;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/user/roles",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class UserRolesController {

    private final UserRolesService userRolesService;

    public UserRolesController(UserRolesService userRolesService) {
        this.userRolesService = userRolesService;
    }

    @GetMapping
    @ApiOperation("Fetch user roles")
    public List<String> getByUserId(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        return userRolesService.retrieveUserRoles(authorisation);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Creates a new User Role")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void save(
        @Valid @NotNull @RequestBody UserRoleRequest userRoleRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        userRolesService.saveRole(userRoleRequest.getRole(), authorisation);
    }
}
