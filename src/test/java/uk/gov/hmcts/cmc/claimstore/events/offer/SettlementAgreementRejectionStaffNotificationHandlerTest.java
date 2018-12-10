package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.staff.SettlementAgreementRejectedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class SettlementAgreementRejectionStaffNotificationHandlerTest {
    private static final SettlementAgreementRejectedEvent event = new SettlementAgreementRejectedEvent(
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

        verify(settlementAgreementRejectedStaffNotificationService, once())
            .notifySettlementRejected(eq(event.getClaim()));
    }
}
