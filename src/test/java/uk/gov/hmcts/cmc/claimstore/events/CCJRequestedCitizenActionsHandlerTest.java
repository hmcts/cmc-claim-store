package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.CCJRequestedNotificationService;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class CCJRequestedCitizenActionsHandlerTest {

    private static final CountyCourtJudgmentRequestedEvent EVENT = new CountyCourtJudgmentRequestedEvent(
        SampleClaimIssuedEvent.CLAIM
    );
    private CCJRequestedCitizenActionsHandler handler;

    @Mock
    CCJRequestedNotificationService ccjRequestedNotificationService;

    @Before
    public void setup() {
        handler = new CCJRequestedCitizenActionsHandler(ccjRequestedNotificationService);
    }

    @Test
    public void notifyClaimantSuccessfully() {
        handler.sendClaimantNotification(EVENT);

        verify(ccjRequestedNotificationService, once()).notifyClaimant(eq(EVENT.getClaim()));
    }

    @Test
    public void notifyDefendantSuccessfullyWhenDefendantEmailProvided() {
        handler.sendDefendantNotification(EVENT);

        verify(ccjRequestedNotificationService, once()).notifyDefendant(eq(EVENT.getClaim()));
    }

    @Test
    public void doNotNotifyDefendantWhenDefendantEmailNotProvided() {

        Claim claimWithoutDefendantEmail = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withDefendants(
                        Arrays.asList(SampleTheirDetails.builder().withEmail(null).individualDetails())
                    ).build()
            ).build();

        handler.sendDefendantNotification(new CountyCourtJudgmentRequestedEvent(claimWithoutDefendantEmail));

        verify(ccjRequestedNotificationService, never()).notifyDefendant(eq(claimWithoutDefendantEmail));
    }
}
