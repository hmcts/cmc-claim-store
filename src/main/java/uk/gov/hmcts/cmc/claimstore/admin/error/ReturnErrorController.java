package uk.gov.hmcts.cmc.claimstore.admin.error;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Useful endpoint to generate errors for testing.
 */
@RestController
public class ReturnErrorController {

    @GetMapping("/admin/error")
    public void error() {
        throw new IllegalStateException("Generated error");
    }
}
