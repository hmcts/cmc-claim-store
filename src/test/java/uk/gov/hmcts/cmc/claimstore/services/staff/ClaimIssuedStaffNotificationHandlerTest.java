package uk.gov.hmcts.cmc.claimstore.services.staff;

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
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class ClaimIssuedStaffNotificationHandlerTest {

    @Mock
    private ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    @Test
    public void notifyStaff() {
        //given
        ClaimIssuedStaffNotificationHandler claimIssuedStaffNotificationHandler
            = new ClaimIssuedStaffNotificationHandler(claimIssuedStaffNotificationService);
        String authorisation = "Bearer Token";
        Claim claim = SampleClaim.getDefault();
        PDF sealedClaim = new PDF("0000-claim", "test".getBytes(), SEALED_CLAIM);
        PDF pinLetterClaim = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);

        DocumentGeneratedEvent generatedEvent
            = new DocumentGeneratedEvent(claim, authorisation, pinLetterClaim, sealedClaim);

        //when
        claimIssuedStaffNotificationHandler.notifyStaff(generatedEvent);

        //verify
        verify(claimIssuedStaffNotificationService)
            .notifyStaffOfClaimIssue(claim, ImmutableList.of(pinLetterClaim, sealedClaim));
    }
}
