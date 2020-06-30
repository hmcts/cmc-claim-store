package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.RESPONSE_DEADLINE;

public class ClaimTest {

    @Test
    public void getScannedDocument() {

        ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
        ScannedDocument expectedScannedDocument = ScannedDocument.builder()
            .documentType(ScannedDocumentType.FORM)
            .subtype(ScannedDocumentSubtype.OCON9X.value)
            .build();
        claimDocumentCollection.addScannedDocument(expectedScannedDocument);

        Claim claim = SampleClaim.getDefault().toBuilder().claimDocumentCollection(claimDocumentCollection).build();

        ScannedDocument scannedDocument =
            claim.getScannedDocument(ScannedDocumentType.FORM, ScannedDocumentSubtype.OCON9X).orElse(null);

        assertThat(scannedDocument).isEqualTo(expectedScannedDocument);
    }

    @Test
    public void isEqualWhenTheSameObjectWithDefaultValuesIsGiven() {
        Claim claim = SampleClaim.getDefault();
        assertThat(claim).isEqualTo(claim);
    }

    @Test
    public void isEqualWhenTheSameObjectWithCustomValuesIsGiven() {
        Claim claim = customValues();

        assertThat(claim).isEqualTo(claim);
    }

    @Test
    public void isNotEqualWhenNullGiven() {
        Claim claim = SampleClaim.getDefault();
        assertThat(claim).isNotEqualTo(null);
    }

    @Test
    public void isNotEqualWhenDifferentTypeObjectGiven() {
        Claim claim = SampleClaim.getDefault();
        assertThat(claim).isNotEqualTo(new HashMap<>());
    }

    @Test
    public void isNotEqualWhenDifferentClaimObjectGiven() {
        Claim claim1 = customValues();
        Claim claim2 = SampleClaim.getDefault();
        assertThat(claim1).isNotEqualTo(claim2);
    }

    @Test
    public void isNotEqualWhenOnlyOneFieldIsDifferent() {
        Claim claim1 = customCreatedAt(NOW_IN_LOCAL_ZONE.plusNanos(1));
        Claim claim2 = customCreatedAt(NOW_IN_LOCAL_ZONE);
        assertThat(claim1).isNotEqualTo(claim2);
    }

    @Test
    public void shouldHaveValidationMessageWhenPaidDateIsInTheFuture() {
        LocalDate futureDate = LocalDate.now().plusDays(100);
        Claim claim = SampleClaim.builder()
            .withMoneyReceivedOn(futureDate)
            .build();

        Set<String> errors = validate(claim);

        assertThat(errors)
            .hasSize(1)
            .contains("moneyReceivedOn : is in the future");
    }

    @Test
    public void shouldHaveNoValidationMessageWhenInstanceIsValidToday() {
        LocalDate today = LocalDate.now();
        Claim claim = SampleClaim.builder()
            .withMoneyReceivedOn(today)
            .build();

        Set<String> errors = validate(claim);

        assertThat(errors)
            .hasSize(0)
            .contains();
    }

    @Test
    public void shouldHaveNoValidationMessageWhenInstanceIsValidPast() {
        LocalDate pastDate = LocalDate.now().minusDays(100);
        Claim claim = SampleClaim.builder()
            .withMoneyReceivedOn(pastDate)
            .build();

        Set<String> errors = validate(claim);

        assertThat(errors)
            .hasSize(0)
            .contains();
    }

    private static Claim customValues() {
        return customCreatedAt(NOW_IN_LOCAL_ZONE);
    }

    private static Claim customCreatedAt(LocalDateTime createdAt) {
        return SampleClaim.builder()
            .withClaimId(1L)
            .withSubmitterId("3")
            .withLetterHolderId("3")
            .withDefendantId("4")
            .withExternalId("external-id")
            .withReferenceNumber("ref number")
            .withCreatedAt(createdAt)
            .withIssuedOn(ISSUE_DATE)
            .withResponseDeadline(RESPONSE_DEADLINE)
            .withMoreTimeRequested(false)
            .withSubmitterEmail("claimant@mail.com")
            .build();
    }
}
