package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;

@RestController
@RequestMapping("/testing-support")
@ConditionalOnProperty("claim-store.test-support.enabled")
public class IntegrationTestSupportController {

    private final ClaimRepository claimRepository;
    private final TestingSupportRepository testingSupportRepository;

    @Autowired
    public IntegrationTestSupportController(
        ClaimRepository claimRepository,
        TestingSupportRepository testingSupportRepository
    ) {
        this.claimRepository = claimRepository;
        this.testingSupportRepository = testingSupportRepository;
    }

    @GetMapping("/claims/{claimReferenceNumber}")
    @ApiOperation("Fetch user claim for given reference number")
    public Claim getByClaimReferenceNumber(
        @PathVariable("claimReferenceNumber") String claimReferenceNumber
    ) {
        return claimRepository.getByClaimReferenceNumber(claimReferenceNumber)
            .orElseThrow(() -> new NotFoundException("Claim not found by ref no: " + claimReferenceNumber));
    }

    @PutMapping("/claims/{claimReferenceNumber}/response-deadline/{newDeadline}")
    @ApiOperation("Manipulate the respond by date of a claim")
    public Claim updateRespondByDate(
        @PathVariable("claimReferenceNumber") String claimReferenceNumber,
        @PathVariable("newDeadline") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDeadline
    ) {
        Claim claim = getByClaimReferenceNumber(claimReferenceNumber);

        testingSupportRepository.updateResponseDeadline(claim.getId(), newDeadline);

        return getByClaimReferenceNumber(claimReferenceNumber);
    }

}
