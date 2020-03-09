package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.services.staff.SettlementReachedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AgreementCountersignedStaffNotificationHandlerTest {

    @Mock
    private SettlementReachedStaffNotificationService settlementReachedStaffNotificationService;
    private AgreementCountersignedStaffNotificationHandler handler;
    private AgreementCountersignedEvent event;

    @BeforeEach
    void setup() {
        event = new AgreementCountersignedEvent(SampleClaim.getWithSettlement(SampleSettlement.validDefaults()),
            MadeBy.CLAIMANT,
            "authorisation");
    }

    @Nested
    @DisplayName("Staff email notification sent")
    class StaffEmailNotificationSent {

        @Test
        void shouldSendStaffNotificationWhenSettlementReached() {
            handler = new AgreementCountersignedStaffNotificationHandler(
                settlementReachedStaffNotificationService);
            handler.onAgreementCountersigned(event);
            verify(settlementReachedStaffNotificationService)
                .notifySettlementReached(any(Claim.class));
        }
    }
}
