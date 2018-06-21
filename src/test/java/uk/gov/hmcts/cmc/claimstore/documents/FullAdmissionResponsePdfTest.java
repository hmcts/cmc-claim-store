package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.FullAdmissionResponseContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.FullDefenceResponseContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.PartyDetailsContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfMeansContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FullAdmissionResponsePdfTest {

    @Mock
    private PDFServiceClient pdfServiceClient;

    private DefendantResponseContentProvider provider = new DefendantResponseContentProvider(
        new PartyDetailsContentProvider(),
        new ClaimDataContentProvider(
            new InterestContentProvider(
                new InterestCalculationService(Clock.systemDefaultZone())
            )
        ),
        new NotificationsProperties(),
        new FullDefenceResponseContentProvider(),
        new FullAdmissionResponseContentProvider(
            new StatementOfMeansContentProvider()
        )
    );
    }
