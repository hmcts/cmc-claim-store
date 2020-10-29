package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.healthcheck.GovNotifyHealthIndicator;
import uk.gov.hmcts.cmc.claimstore.healthcheck.PDFServiceHealthIndicator;
import uk.gov.hmcts.reform.docassembly.healthcheck.DocAssemblyHealthIndicator;
import uk.gov.hmcts.reform.document.healthcheck.DocumentManagementHealthIndicator;
import uk.gov.hmcts.reform.sendletter.healthcheck.SendLetterHealthIndicator;

import java.util.List;

import static java.util.Arrays.asList;
import static org.springframework.boot.actuate.health.Status.UP;

@Api
@RestController
public class HealthCheckController {

    private final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    private final List<HealthIndicator> healthIndicators;

    @Autowired
    public HealthCheckController(GovNotifyHealthIndicator govNotifyHealthIndicator,
                                 SendLetterHealthIndicator sendLetterHealthIndicator,
                                 PDFServiceHealthIndicator pdfServiceHealthIndicator,
                                 DocAssemblyHealthIndicator docAssemblyHealthIndicator,
                                 DocumentManagementHealthIndicator documentManagementHealthIndicator) {
        healthIndicators = asList(govNotifyHealthIndicator,
            pdfServiceHealthIndicator, docAssemblyHealthIndicator,
            documentManagementHealthIndicator, sendLetterHealthIndicator);
    }

    @GetMapping("/healthy")
    @ApiOperation("Check health of services required for Claim Issue")
    public Boolean check() {

        boolean isHealthy = healthIndicators.stream()
            .allMatch(healthIndicator -> UP.equals(healthIndicator.health().getStatus()));

        if (!isHealthy) {
            logger.info("Some of the services required to issue claim are not in healthy state.");
        }

        return isHealthy;
    }

}
