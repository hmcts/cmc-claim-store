package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class ClaimSubmissionOperationIndicatorRuleTest {

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

        try {
            new ClaimSubmissionOperationIndicatorRule().assertOperationIndicatorUpdateIsValid(claim, input);
            Assert.fail("Expected a BadRequestException to be thrown");
        } catch (BadRequestException expected) {
            assertThat(expected).hasMessage("Invalid input. The following indicator(s)[claimantNotification, "
                + "defendantNotification, bulkPrint, rpa, staffNotification, sealedClaimUpload, "
                + "claimIssueReceiptUpload] "
                + "cannot be set to Yes");
        }

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

        try {
            new ClaimSubmissionOperationIndicatorRule().assertOperationIndicatorUpdateIsValid(claim, input);
            Assert.fail("Expected a BadRequestException to be thrown");
        } catch (BadRequestException expected) {
            assertThat(expected).hasMessage("Invalid input. The following indicator(s)[sealedClaimUpload,"
                + " claimIssueReceiptUpload] cannot be set to NO");
        }
    }
}
