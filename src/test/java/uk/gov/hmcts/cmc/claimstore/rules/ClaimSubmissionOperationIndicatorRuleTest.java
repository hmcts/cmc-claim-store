package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class ClaimSubmissionOperationIndicatorRuleTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void shouldAssertOperationIndicatorUpdateIsInvalid() {
        Claim claim = SampleClaim.getDefault();
        final ClaimSubmissionOperationIndicators input = ClaimSubmissionOperationIndicators
            .builder()
            .claimIssueReceiptUpload(YES)
            .sealedClaimUpload(YES)
            .bulkPrint(YES)
            .claimantNotification(YES)
            .rpa(YES)
            .defendantNotification(YES)
            .staffNotification(YES)
            .build();

        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("Invalid input. The following indicator(s)[claimantNotification, "
            + "defendantNotification, bulkPrint, rpa, staffNotification, sealedClaimUpload, claimIssueReceiptUpload] "
            + "cannot be set to Yes");

        new ClaimSubmissionOperationIndicatorRule().assertOperationIndicatorUpdateIsValid(claim, input);
    }
}
