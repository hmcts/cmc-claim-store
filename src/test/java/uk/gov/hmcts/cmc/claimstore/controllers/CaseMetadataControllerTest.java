package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.support.CaseMetadataController;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.domain.models.CaseMetadata;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseMetadataControllerTest {

    @Mock
    private ClaimService claimService;

    @Mock
    private UserService userService;

    private CaseMetadataController controller;

    private Claim sampleClaim;

    @Before
    public void setup() {
        controller = new CaseMetadataController(claimService, userService);
        sampleClaim = SampleClaim.getDefault();
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(SampleUser.getDefault());
    }

    @Test
    public void shouldReturnClaimsBySubmitterId() {
        // given
        when(claimService.getClaimBySubmitterId(eq("submitter"), anyString()))
            .thenReturn(singletonList(sampleClaim));

        // when
        List<CaseMetadata> output = controller.getBySubmitterId("submitter");

        // then
        assertEquals(1, output.size());
        assertValid(sampleClaim, output.get(0));
    }

    @Test
    public void shouldReturnClaimsByDefendantId() {
        // given
        when(claimService.getClaimByDefendantId(eq("defendant"), anyString()))
            .thenReturn(singletonList(sampleClaim));

        // when
        List<CaseMetadata> output = controller.getByDefendantId("defendant");

        // then
        assertEquals(1, output.size());
        assertValid(sampleClaim, output.get(0));
    }

    @Test
    public void shouldReturnClaimByExternalId() {
        // given
        when(claimService.getClaimByReferenceAnonymous("reference"))
            .thenReturn(Optional.of(sampleClaim));

        // when
        CaseMetadata output = controller.getByClaimReference("reference");

        // then
        assertNotNull(output);
        assertValid(sampleClaim, output);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenNoClaimWithReference() {
        // given
        when(claimService.getClaimByReferenceAnonymous("reference"))
            .thenReturn(Optional.empty());

        // when
        controller.getByClaimReference("reference");

        // then exception should have been thrown
    }

    @Test
    public void shouldReturnClaimsByClaimantEmail() {
        // given
        when(claimService.getClaimByClaimantEmail(eq("claimant@server.net"), anyString()))
            .thenReturn(singletonList(sampleClaim));

        // when
        List<CaseMetadata> output = controller.getByClaimantEmailFilter("claimant@server.net");

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
        List<CaseMetadata> output = controller.getByDefendantEmailFilter("defendant@server.net");

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
        List<CaseMetadata> output = controller.getByPaymentReference("RC-1234-5678-0123-4567");

        // then
        assertEquals(1, output.size());
        assertValid(sampleClaim, output.get(0));
    }


    private static void assertValid(Claim dto, CaseMetadata metadata) {
        assertEquals(dto.getId(), metadata.getId());
        assertEquals(dto.getSubmitterId(), metadata.getSubmitterId());
        assertEquals(dto.getDefendantId(), metadata.getDefendantId());
        assertEquals(dto.getExternalId(), metadata.getExternalId());
        assertEquals(dto.getReferenceNumber(), metadata.getReferenceNumber());
        assertEquals(dto.getCreatedAt(), metadata.getCreatedAt());
        assertEquals(dto.getIssuedOn(), metadata.getIssuedOn());
        assertEquals(dto.getResponseDeadline(), metadata.getResponseDeadline());
        assertEquals(dto.getRespondedAt(), metadata.getRespondedAt());
        assertEquals(dto.isMoreTimeRequested(), metadata.isMoreTimeRequested());
        assertEquals(dto.getCountyCourtJudgmentRequestedAt(), metadata.getCountyCourtJudgmentRequestedAt());
        assertEquals(dto.getSettlementReachedAt(), metadata.getSettlementReachedAt());
        assertEquals(dto.getSealedClaimDocument(), Optional.ofNullable(metadata.getSealedClaimDocument()));
        assertEquals(dto.getClaimData().getPayment().getReference(), metadata.getPaymentReference());
        assertEquals(dto.getMoneyReceivedOn(), metadata.getMoneyReceivedOn());
    }
}
