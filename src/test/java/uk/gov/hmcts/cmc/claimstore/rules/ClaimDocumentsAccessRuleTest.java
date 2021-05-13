package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClaimDocumentsAccessRuleTest {

    private static final User DEFENDANT = SampleUser.getDefaultDefendant();
    private static final User DEFENDANT_ANONYMOUS = SampleUser.getDefaultDefendantWithLetterHolder();
    private static final User CASEWORKER = SampleUser.getDefaultCaseworker();
    private static final User CLAIMANT = SampleUser.getDefaultClaimant();
    private static final User SOLICITOR = SampleUser.getDefaultSolicitor();
    private static final Claim CLAIM = SampleClaim.getDefault();
    private static final User UNRELATED = SampleUser.builder()
        .withUserDetails(SampleUserDetails.builder().withUserId("10001").withMail("kk@mm.com").build())
        .build();

    @Test
    void failsIfClaimOrUserIsNull() {

        assertAll(
            () -> assertThrows(ForbiddenActionException.class, () -> ClaimDocumentsAccessRule
                .assertDocumentCanBeAccessedByUser(null, null, null)),

            () -> assertThrows(ForbiddenActionException.class, () -> ClaimDocumentsAccessRule
                .assertDocumentCanBeAccessedByUser(null, ClaimDocumentType.ORDER_DIRECTIONS, DEFENDANT)),

            () -> assertThrows(ForbiddenActionException.class, () -> ClaimDocumentsAccessRule
                .assertDocumentCanBeAccessedByUser(CLAIM, ClaimDocumentType.ORDER_DIRECTIONS, null))
        );
    }

    @Test
    void failsWhenDefendantAccessClaimReceipt() {
        Claim claimWithDefendant = CLAIM.toBuilder().submitterId(DEFENDANT.getUserDetails().getId()).build();
        assertThrows(ForbiddenActionException.class, () -> ClaimDocumentsAccessRule
            .assertDocumentCanBeAccessedByUser(claimWithDefendant, ClaimDocumentType.CLAIM_ISSUE_RECEIPT, DEFENDANT)
        );
    }

    @Test
    void failsWhenClaimantAccessSealedClaim() {
        Claim claimWithDefendant = CLAIM.toBuilder().submitterId(CLAIMANT.getUserDetails().getId()).build();
        assertThrows(ForbiddenActionException.class, () -> ClaimDocumentsAccessRule
            .assertDocumentCanBeAccessedByUser(claimWithDefendant, ClaimDocumentType.SEALED_CLAIM, CLAIMANT)
        );
    }

    @ParameterizedTest
    @EnumSource(
        value = ClaimDocumentType.class,
        names = {"SEALED_CLAIM"},
        mode = EnumSource.Mode.EXCLUDE)
    void allowsAllClaimantAccessibleDocuments(ClaimDocumentType documentType) {
        Claim claimWithDefendant = CLAIM.toBuilder().submitterId(CLAIMANT.getUserDetails().getId()).build();
        ClaimDocumentsAccessRule.assertDocumentCanBeAccessedByUser(claimWithDefendant, documentType, CLAIMANT);
    }

    @ParameterizedTest
    @EnumSource(
        value = ClaimDocumentType.class,
        names = {"CLAIM_ISSUE_RECEIPT"},
        mode = EnumSource.Mode.EXCLUDE)
    void allowsAllDefendantAccessibleDocuments(ClaimDocumentType documentType) {
        Claim claimWithDefendant = CLAIM.toBuilder().submitterId(DEFENDANT.getUserDetails().getId()).build();
        ClaimDocumentsAccessRule.assertDocumentCanBeAccessedByUser(claimWithDefendant, documentType, DEFENDANT);
    }

    @ParameterizedTest
    @EnumSource(
        value = ClaimDocumentType.class,
        names = {"CLAIM_ISSUE_RECEIPT"},
        mode = EnumSource.Mode.EXCLUDE)
    void allowsAllDefendantAccessibleDocumentsWhenDefendantIDNull(ClaimDocumentType documentType) {
        Claim claimWithDefendant = CLAIM.toBuilder().defendantId(null).build();
        ClaimDocumentsAccessRule.assertDocumentCanBeAccessedByUser(claimWithDefendant,
            documentType, DEFENDANT_ANONYMOUS);
    }

    @ParameterizedTest
    @EnumSource(
        value = ClaimDocumentType.class,
        names = {"SEALED_CLAIM"},
        mode = EnumSource.Mode.INCLUDE)
    void allowsSealedClaimAccessibleToSolicitor(ClaimDocumentType documentType) {
        Claim claimWithDefendant = CLAIM.toBuilder().submitterId(SOLICITOR.getUserDetails().getId()).build();
        ClaimDocumentsAccessRule.assertDocumentCanBeAccessedByUser(claimWithDefendant, documentType, SOLICITOR);
    }

    @ParameterizedTest
    @EnumSource(
        value = ClaimDocumentType.class,
        names = {"CCJ_REQUEST", "CLAIM_ISSUE_RECEIPT", "SEALED_CLAIM",
        "DEFENDANT_PIN_LETTER", "DEFENDANT_RESPONSE_RECEIPT"},
        mode = EnumSource.Mode.INCLUDE)
    void allowsAllClaimDocumentsAccessibleToCaseworker(ClaimDocumentType documentType) {
        Claim claimWithDefendant = CLAIM.toBuilder().submitterId(SOLICITOR.getUserDetails().getId()).build();
        ClaimDocumentsAccessRule.assertDocumentCanBeAccessedByUser(claimWithDefendant, documentType, CASEWORKER);
    }

    @ParameterizedTest
    @EnumSource(
        value = ClaimDocumentType.class,
        names = {"SEALED_CLAIM"},
        mode = EnumSource.Mode.EXCLUDE)
    void blockDocsOtherThanSealedClaimForSolicitor(ClaimDocumentType documentType) {
        Claim claimWithDefendant = CLAIM.toBuilder().submitterId(SOLICITOR.getUserDetails().getId()).build();
        assertThrows(ForbiddenActionException.class, () ->
            ClaimDocumentsAccessRule.assertDocumentCanBeAccessedByUser(claimWithDefendant, documentType, SOLICITOR)
        );
    }

    @ParameterizedTest
    @EnumSource(
        value = ClaimDocumentType.class)
    void blockAllDocumentsForUnrelatedUser(ClaimDocumentType documentType) {
        Claim claimWithDefendant = CLAIM.toBuilder().submitterId(DEFENDANT.getUserDetails().getId()).build();
        assertThrows(ForbiddenActionException.class, () -> ClaimDocumentsAccessRule
            .assertDocumentCanBeAccessedByUser(claimWithDefendant, documentType, UNRELATED));
    }

}
