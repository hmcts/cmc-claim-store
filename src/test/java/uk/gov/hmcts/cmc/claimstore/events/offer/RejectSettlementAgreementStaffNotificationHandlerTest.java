package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.settlement.RejectSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.RejectSettlementAgreementStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RejectSettlementAgreementStaffNotificationHandlerTest {
    private static final RejectSettlementAgreementEvent event = new RejectSettlementAgreementEvent(
        SampleClaim.getClaimWithSettlementAgreementRejected());

    private RejectSettlementAgreementStaffNotificationHandler handler;

    @Mock
    private RejectSettlementAgreementStaffNotificationService rejectSettlementAgreementStaffNotificationService;

    @Before
    public void setUp() {
        handler = new RejectSettlementAgreementStaffNotificationHandler(
            rejectSettlementAgreementStaffNotificationService
        );
    }

    @Test
    public void notifyStaffClaimantResponseStatesPaidSubmittedFor() {
        handler.onSettlementAgreementRejected(event);

        verify(rejectSettlementAgreementStaffNotificationService)
            .notifySettlementRejected(event.getClaim());
    }
}
