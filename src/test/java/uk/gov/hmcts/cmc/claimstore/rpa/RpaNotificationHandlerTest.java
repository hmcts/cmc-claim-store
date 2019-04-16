package uk.gov.hmcts.cmc.claimstore.rpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class RpaNotificationHandlerTest {
    @Mock
    private ClaimIssuedNotificationService bulkPrintService;

    @Test
    public void notifyRpa() {
        //given
        String authorisation = "Bearer Token";
        Claim claim = SampleClaim.getDefault();
        PDF sealedClaim = new PDF("0000-claim", "test".getBytes(), SEALED_CLAIM);
        DocumentGeneratedEvent generatedEvent = new DocumentGeneratedEvent(claim, authorisation, sealedClaim);
        RpaNotificationHandler rpaNotificationHandler = new RpaNotificationHandler(bulkPrintService);

        //when
        rpaNotificationHandler.notifyRobotOfClaimIssue(generatedEvent);

        //verify
        verify(bulkPrintService).notifyRobotics(claim, ImmutableList.of(sealedClaim));
    }

}
