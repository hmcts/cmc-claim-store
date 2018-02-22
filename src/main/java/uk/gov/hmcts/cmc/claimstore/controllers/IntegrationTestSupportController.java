package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.repositories.support.SupportRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;

@RestController
@RequestMapping("/testing-support")
@ConditionalOnProperty("claim-store.test-support.enabled")
public class IntegrationTestSupportController {

    private final SupportRepository supportRepository;

    @Autowired
    public IntegrationTestSupportController(SupportRepository supportRepository) {
        this.supportRepository = supportRepository;
    }

    @GetMapping("/trigger-server-error")
    public void throwAnError() {
        throw new IllegalStateException("Something really bad happened!");
    }

    @GetMapping("/claims/{claimReferenceNumber}")
    @ApiOperation("Fetch user claim for given reference number")
    public Claim getByClaimReferenceNumber(
        @PathVariable("claimReferenceNumber") String claimReferenceNumber,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorisation
    ) {
        return getClaim(claimReferenceNumber, authorisation);
    }

    @PutMapping("/claims/{claimReferenceNumber}/response-deadline/{newDeadline}")
    @ApiOperation("Manipulate the respond by date of a claim")
    public Claim updateRespondByDate(
        @PathVariable("claimReferenceNumber") String claimReferenceNumber,
        @PathVariable("newDeadline") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDeadline,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorisation
    ) {
        Claim claim = getClaim(claimReferenceNumber, authorisation);

        supportRepository.updateResponseDeadline(authorisation, claim, newDeadline);

        return getClaim(claimReferenceNumber, authorisation);
    }

    private Claim getClaim(String claimReferenceNumber, String authorisation) {
        return supportRepository.getByClaimReferenceNumber(claimReferenceNumber, authorisation)
            .orElseThrow(() -> new NotFoundException("Claim not found by ref no: " + claimReferenceNumber));
    }

}
