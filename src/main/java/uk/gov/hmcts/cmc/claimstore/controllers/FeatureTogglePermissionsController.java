package uk.gov.hmcts.cmc.claimstore.controllers;

    import io.swagger.annotations.Api;
    import io.swagger.annotations.ApiOperation;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.MediaType;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestHeader;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(
    path = "/users",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class FeatureTogglePermissionsController {

    @GetMapping("/roles")
    @ApiOperation("Fetch user roles")
    public String getBySubmitterId(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        return "cmc-new-features-consent-given, citizen, admin, some-role";
    }
}
