package uk.gov.hmcts.cmc.claimstore.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    /*
     * Azure hits us on / every 5 seconds to prevent it sleeping the application
     * Application insights registers that as a 404 and adds it as an exception,
     * This is here to reduce the noise
     */
    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public void root() {
        // Only used for returning a 200 on /
    }
}
