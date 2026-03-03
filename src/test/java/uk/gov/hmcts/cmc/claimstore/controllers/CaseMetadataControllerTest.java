package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.support.CaseMetadataController;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.metadata.CaseMetadata;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.CREATE;

@RunWith(MockitoJUnitRunner.class)
public class CaseMetadataControllerTest {

    @Mock
    private ClaimService claimService;

    @Mock
    private UserService userService;

    private CaseMetadataController controller;

    private Claim sampleClaim;

    private Claim sampleRepresentedClaim;

    @Before
    public void setup() {
        controller = new CaseMetadataController(claimService, userService);
        sampleClaim = SampleClaim.getDefault();
        sampleRepresentedClaim = SampleClaim.getDefaultForLegal();
    }

    @Test
    public void shouldReturnClaimsBySubmitterId() {
        // given
        when(claimService.getClaimBySubmitterId(eq("submitter"), anyString(), Mockito.anyInt()))
            .thenReturn(singletonList(sampleClaim));

        // when
        List<CaseMetadata> output = controller.getBySubmitterId("submitter", "authorisation");

        // then
        assertEquals(1, output.size());
        assertValid(sampleClaim, output.get(0));
    }

    @Test
    public void shouldReturnClaimsByDefendantId() {
        // given
        when(claimService.getClaimByDefendantId(eq("defendant"), anyString(), Mockito.anyInt()))
            .thenReturn(singletonList(sampleClaim));

        // when
        List<CaseMetadata> output = controller.getByDefendantId("defendant", "authorisation");

        // then
        assertEquals(1, output.size());
        assertValid(sampleClaim, output.get(0));
    }

    @Test
    public void shouldReturnClaimByExternalId() {
        // given
        when(claimService.getClaimByExternalId("externalId", "authorisation"))
            .thenReturn(sampleClaim);

        // when
        CaseMetadata output = controller.getByExternalId("externalId", "authorisation");

        // then
        assertNotNull(output);
        assertValid(sampleClaim, output);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenNoClaimWithReference() {
        // given
        when(claimService.getClaimByReference("reference", "authorisation"))
            .thenReturn(Optional.empty());

        // when
        controller.getByClaimReference("reference", "authorisation");

        // then exception should have been thrown
    }

    @Test
    public void shouldReturnClaimsByClaimantEmail() {
        // given
        when(claimService.getClaimByClaimantEmail(eq("claimant@server.net"), anyString()))
            .thenReturn(singletonList(sampleClaim));

        // when
        List<CaseMetadata> output = controller.getByClaimantEmailFilter("claimant@server.net", "authorisation");

        // then
        assertEquals(1, output.size());
        assertValid(sampleClaim, output.get(0));
    }

    @Test
    public void shouldReturnClaimsByDefendantEmail() {
        // given
        when(claimService.getClaimByDefendantEmail(eq("defendant@server.net"), anyString()))
            .thenReturn(singletonList(sampleClaim));

        // when
        List<CaseMetadata> output = controller.getByDefendantEmailFilter("defendant@server.net", "authorisation");

        // then
        assertEquals(1, output.size());
        assertValid(sampleClaim, output.get(0));
    }

    @Test
    public void shouldReturnClaimsByPaymentReference() {
        // given
        when(claimService.getClaimByPaymentReference(eq("RC-1234-5678-0123-4567"), anyString()))
            .thenReturn(singletonList(sampleClaim));

        // when
        List<CaseMetadata> output = controller.getByPaymentReference("RC-1234-5678-0123-4567", "authorisation");

        // then
        assertEquals(1, output.size());
        assertValid(sampleClaim, output.get(0));
    }

    @Test
    public void shouldReturnRepresentedClaimByClaimReference() {
        // given
        when(claimService.getClaimByReference(sampleRepresentedClaim.getReferenceNumber(), "authorisation"))
            .thenReturn(Optional.of(sampleRepresentedClaim));

        // when
        CaseMetadata output = controller.getByClaimReference(sampleRepresentedClaim.getReferenceNumber(), "authorisation");

        // then
        assertNotNull(output);
        assertValid(sampleRepresentedClaim, output);
    }

    @Test
    public void shouldReturnClaimsWithCreatedState() {
        // given
        sampleClaim = SampleClaim.getDefault();
        when(userService.getUser("authorisation")).thenReturn(null);
        when(claimService.getClaimsByState(eq(CREATE), any()))
            .thenReturn(singletonList(SampleClaim.builder().withState(CREATE).build()));

        // when
        List<CaseMetadata> cases = controller.getCreatedCases("authorisation");

        // then
        assertEquals(1, cases.size());
        assertEquals(CREATE, cases.get(0).getState());
        assertValid(sampleClaim, cases.get(0));
    }

    private static void assertValid(Claim dto, CaseMetadata metadata) {
        assertEquals(dto.getId(), metadata.getId());
        assertEquals(dto.getSubmitterId(), metadata.getSubmitterId());
        assertEquals(
            dto.getClaimData().getClaimant().getClass().getSimpleName(),
            metadata.getSubmitterPartyTypes().get(0));
        assertEquals(dto.getDefendantId(), metadata.getDefendantId());
        assertEquals(
            dto.getClaimData().getDefendant().getClass().getSimpleName(),
            metadata.getDefendantPartyTypes().get(0));
        assertEquals(dto.getExternalId(), metadata.getExternalId());
        assertEquals(dto.getReferenceNumber(), metadata.getReferenceNumber());
        assertEquals(dto.getCreatedAt(), metadata.getCreatedAt());
        assertEquals(dto.getIssuedOn().orElseThrow(), metadata.getIssuedOn());
        assertEquals(dto.getResponseDeadline(), metadata.getResponseDeadline());
        assertEquals(dto.isMoreTimeRequested(), metadata.getMoreTimeRequested());
        assertEquals(dto.getIntentionToProceedDeadline(), metadata.getIntentionToProceedDeadline());

        assertEquals(
            dto.getClaimDocument(SEALED_CLAIM).map(ClaimDocument::getDocumentManagementUrl).orElse(null),
            metadata.getSealedClaimDocument());
        assertEquals(dto.getMoneyReceivedOn().orElse(null), metadata.getMoneyReceivedOn());

        dto.getClaimData().getPayment()
            .ifPresent(payment -> assertEquals(payment.getReference(), metadata.getPaymentReference()));

        if (!dto.getClaimData().getPayment().isPresent()) {
            assertNull(metadata.getPaymentReference());
        }
    }
}
