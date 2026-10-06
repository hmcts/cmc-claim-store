package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.claimstore.rpa.BreathingSpaceNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@ExtendWith(MockitoExtension.class)
public class RpaOperationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    public static final PDF pinLetterClaim = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);
    public static final PDF sealedClaim = new PDF("0000-sealed-claim", "test".getBytes(), SEALED_CLAIM);

    private RpaOperationService rpaOperationService;
    @Mock
    private ClaimIssuedNotificationService claimIssuedNotificationService;
    @Mock
    private ClaimCreationEventsStatusService eventsStatusService;

    @Mock
    private BreathingSpaceNotificationService breathingSpaceNotificationService;

    @BeforeEach
    public void before() {
        rpaOperationService = new RpaOperationService(claimIssuedNotificationService,
            breathingSpaceNotificationService, eventsStatusService);
    }

    @Test
    public void shouldNotifyCitizen() {
        //when
        rpaOperationService.notify(CLAIM, AUTHORISATION, pinLetterClaim, sealedClaim);

        //verify
        verify(claimIssuedNotificationService).notifyRobotics(
            CLAIM,
            ImmutableList.of(pinLetterClaim, sealedClaim)
        );
    }

    @Test
    public void shouldNotifyBreathingSpace() {
        //when
        rpaOperationService.notifyBreathingSpace(CLAIM, AUTHORISATION, pinLetterClaim, sealedClaim);

        //verify
        verify(breathingSpaceNotificationService).notifyRobotics(
            eq(CLAIM),
            eq(ImmutableList.of(pinLetterClaim, sealedClaim))
        );
    }
}
