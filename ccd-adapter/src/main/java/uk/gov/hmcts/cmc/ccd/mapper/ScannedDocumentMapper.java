package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocumentType;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentType;

import java.net.URI;

@Component
public class ScannedDocumentMapper {

    public CCDCollectionElement<CCDScannedDocument> to(ScannedDocument scannedDocument) {
        CCDScannedDocument.CCDScannedDocumentBuilder builder = CCDScannedDocument.builder();

        builder.controlNumber(scannedDocument.getControlNumber())
            .deliveryDate(scannedDocument.getDeliveryDate())
            .type(CCDScannedDocumentType.valueOf(scannedDocument.getDocumentType().name().toLowerCase()))
            .exceptionRecordReference(scannedDocument.getExceptionRecordReference())
            .fileName(scannedDocument.getFileName())
            .scannedDate(scannedDocument.getScannedDate())
            .subtype(scannedDocument.getSubtype())
            .url(CCDDocument
                .builder()
                .documentUrl(scannedDocument.getDocumentManagementUrl().toString())
                .documentBinaryUrl(scannedDocument.getDocumentManagementBinaryUrl().toString())
                .documentFileName(scannedDocument.getFileName())
                .build()
            );

        return CCDCollectionElement.<CCDScannedDocument>builder()
            .value(builder.build())
            .id(scannedDocument.getId())
            .build();
    }

    public ScannedDocument from(CCDCollectionElement<CCDScannedDocument> collectionElement) {

        if (collectionElement == null || collectionElement.getValue() == null) {
            return null;
        }

        CCDScannedDocument ccdScannedDocument = collectionElement.getValue();

        return ScannedDocument.builder()
            .id(collectionElement.getId())
            .fileName(ccdScannedDocument.getFileName())
            .documentManagementUrl(URI.create(ccdScannedDocument.getUrl().getDocumentUrl()))
            .documentManagementBinaryUrl(URI.create(ccdScannedDocument.getUrl().getDocumentBinaryUrl()))
            .documentType(ScannedDocumentType.valueOf(ccdScannedDocument.getType().name().toUpperCase()))
            .scannedDate(ccdScannedDocument.getScannedDate())
            .deliveryDate(ccdScannedDocument.getDeliveryDate())
            .subtype(ccdScannedDocument.getSubtype())
            .exceptionRecordReference(ccdScannedDocument.getExceptionRecordReference())
            .controlNumber(ccdScannedDocument.getControlNumber())
            .build();
    }
}
