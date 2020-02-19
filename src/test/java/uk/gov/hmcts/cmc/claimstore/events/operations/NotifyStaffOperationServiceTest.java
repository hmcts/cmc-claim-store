package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class NotifyStaffOperationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    public static final PDF sealedClaim = new PDF("0000-sealed-claim", "test".getBytes(), SEALED_CLAIM);

    private NotifyStaffOperationService notifyStaffOperationService;

    @Mock
    private ClaimCreationEventsStatusService eventsStatusService;

    @Before
    public void before() {
        notifyStaffOperationService = new NotifyStaffOperationService(eventsStatusService);
    }

    @Test
    public void updateClaimOperation() {
        //when
        notifyStaffOperationService.notify(CLAIM, AUTHORISATION);

        //verify
        verify(eventsStatusService).updateClaimOperationCompletion(
            eq(AUTHORISATION),
            eq(CLAIM),
            eq(CaseEvent.PIN_GENERATION_OPERATIONS)
        );
    }
}
