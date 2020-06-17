package uk.gov.hmcts.cmc.claimstore.documents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;

import java.util.List;

@Service("bulkPrintService")
@ConditionalOnProperty(prefix = "send-letter", name = "url", havingValue = "false")
public class DummyBulkPrintService implements PrintService {
    private static final Logger logger = LoggerFactory.getLogger(DummyBulkPrintService.class);

    @Override
    public BulkPrintDetails printHtmlLetter(Claim claim, List<Printable> documents,
                                            BulkPrintRequestType type, String authorisation) {
        logger.info("No bulk print operation need to be performed as 'Bulk print url' is switched off.");
        return BulkPrintDetails.builder().build();
    }

    @Override
    public BulkPrintDetails printPdf(Claim claim, List<Printable> documents,
                                     BulkPrintRequestType type, String authorisation) {
        logger.info("No bulk print operation need to be performed as 'Bulk print url' is switched off.");
        return BulkPrintDetails.builder().build();
    }
}
