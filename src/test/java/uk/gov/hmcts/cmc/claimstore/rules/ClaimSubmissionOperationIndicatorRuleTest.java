package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.net.URI;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class ClaimSubmissionOperationIndicatorRuleTest {
    @Rule
    public final ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void shouldAssertOperationIndicatorUpdateIsInvalid() {
        Claim claim = SampleClaim.builder().build();
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

    @Test
    public void shouldFailWhenDocumentsArePresentAndAreRequestedForUpload() {
        Claim claim = SampleClaim.builder()
            .withSealedClaimDocument(URI.create("SealedClaim"))
            .withClaimIssueReceiptDocument(URI.create("ClaimIssueReceipt"))
            .withClaimSubmissionOperationIndicators(ClaimSubmissionOperationIndicators
                .builder()
                .sealedClaimUpload(YES)
                .claimIssueReceiptUpload(YES)
                .defendantNotification(YES)
                .claimantNotification(YES)
                .staffNotification(YES)
                .rpa(YES)
                .bulkPrint(YES)
                .build()
            )
            .build();

        final ClaimSubmissionOperationIndicators input = ClaimSubmissionOperationIndicators
            .builder()
            .claimIssueReceiptUpload(NO)
            .sealedClaimUpload(NO)
            .bulkPrint(YES)
            .claimantNotification(YES)
            .rpa(YES)
            .defendantNotification(YES)
            .staffNotification(YES)
            .build();

        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("Invalid input. The following indicator(s)[sealedClaimUpload,"
            + " claimIssueReceiptUpload] cannot be set to NO");

        new ClaimSubmissionOperationIndicatorRule().assertOperationIndicatorUpdateIsValid(claim, input);
    }
}
