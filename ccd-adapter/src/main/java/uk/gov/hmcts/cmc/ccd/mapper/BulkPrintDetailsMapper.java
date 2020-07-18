package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDBulkPrintDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDPrintRequestType;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.domain.models.bulkprint.PrintRequestType;

@Component
public class BulkPrintDetailsMapper implements Mapper<CCDCollectionElement<CCDBulkPrintDetails>, BulkPrintDetails> {

    @Override
    public CCDCollectionElement<CCDBulkPrintDetails> to(BulkPrintDetails bulkPrintDetails) {
        if (bulkPrintDetails == null) {
            return null;
        }

        return CCDCollectionElement.<CCDBulkPrintDetails>builder()
            .value(CCDBulkPrintDetails.builder()
                .printRequestId(bulkPrintDetails.getPrintRequestId())
                .printRequestType(CCDPrintRequestType.valueOf(bulkPrintDetails.getPrintRequestType().name()))
                .printRequestedAt(bulkPrintDetails.getPrintRequestedAt())
                .build())
            .id(bulkPrintDetails.getId())
            .build();
    }

    @Override
    public BulkPrintDetails from(CCDCollectionElement<CCDBulkPrintDetails> collectionElement) {
        CCDBulkPrintDetails bulkPrintDetails = collectionElement.getValue();

        if (bulkPrintDetails == null) {
            return null;
        }

        return BulkPrintDetails
            .builder()
            .id(collectionElement.getId())
            .printRequestId(bulkPrintDetails.getPrintRequestId())
            .printRequestType(PrintRequestType.valueOf(bulkPrintDetails.getPrintRequestType().name()))
            .printRequestedAt(bulkPrintDetails.getPrintRequestedAt())
            .build();
    }
}
