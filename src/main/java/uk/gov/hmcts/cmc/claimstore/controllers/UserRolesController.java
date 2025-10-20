package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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

@Tag(name = "User Roles Controller")
@RestController
@Validated
@RequestMapping(
    path = "/user/roles",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class UserRolesController {

    private final UserRolesService userRolesService;

    public UserRolesController(UserRolesService userRolesService) {
        this.userRolesService = userRolesService;
    }

    @GetMapping
    @Operation(summary = "Fetch user roles")
    public List<String> getByUserId(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        return userRolesService.retrieveUserRoles(authorisation);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates a new User Role")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void save(
        @Valid @NotNull @RequestBody UserRoleRequest userRoleRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        userRolesService.saveRole(userRoleRequest.getRole(), authorisation);
    }
}
