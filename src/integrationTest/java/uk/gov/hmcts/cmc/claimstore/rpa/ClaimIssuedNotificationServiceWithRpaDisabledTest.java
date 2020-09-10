package uk.gov.hmcts.cmc.claimstore.rpa;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@SpringBootTest(properties = {"feature_toggles.legal_sealed_claim_for_rpa_enabled=false"})
public class ClaimIssuedNotificationServiceWithRpaDisabledTest extends BaseMockSpringTest {
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private ClaimIssuedNotificationService service;

    @MockBean
    protected EmailService emailService;

    @Test
    public void shouldSendEmailToLegalSealedClaimRecipient() {
        Claim claim = SampleClaim.getLegalSealedClaim();
        List<PDF> documents = new ArrayList<>();
        PDF sealedClaimDoc = new PDF(buildSealedClaimFileBaseName(SampleClaim.LEGAL_REFERENCE_NUMBER),
            PDF_CONTENT,
            SEALED_CLAIM);
        PDF defendantLetterDoc = new PDF(buildDefendantLetterFileBaseName(SampleClaim.LEGAL_REFERENCE_NUMBER),
            PDF_CONTENT,
            DEFENDANT_PIN_LETTER);

        documents.add(defendantLetterDoc);
        documents.add(sealedClaimDoc);

        service.notifyRobotics(claim, documents);

        verify(emailService, never()).sendEmail(anyString(), any());
    }
}
