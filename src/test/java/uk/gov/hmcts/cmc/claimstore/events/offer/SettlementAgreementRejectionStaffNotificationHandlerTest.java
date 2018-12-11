package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.settlement.RejectSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.SettlementAgreementRejectedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SettlementAgreementRejectionStaffNotificationHandlerTest {
    private static final RejectSettlementAgreementEvent event = new RejectSettlementAgreementEvent(
        SampleClaim.getClaimWithSettlementAgreementRejected());

    private SettlementAgreementRejectionStaffNotificationHandler handler;

    @Mock
    private SettlementAgreementRejectedStaffNotificationService settlementAgreementRejectedStaffNotificationService;

    @Before
    public void setUp() {
        handler = new SettlementAgreementRejectionStaffNotificationHandler(
            settlementAgreementRejectedStaffNotificationService
        );
    }

    @Test
    public void notifyStaffClaimantResponseStatesPaidSubmittedFor() {
        handler.onSettlementAgreementRejected(event);

        verify(settlementAgreementRejectedStaffNotificationService)
            .notifySettlementRejected(event.getClaim());
    }
}
