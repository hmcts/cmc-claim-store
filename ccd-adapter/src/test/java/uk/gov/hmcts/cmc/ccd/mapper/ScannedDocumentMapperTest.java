package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocumentType;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentType;

import java.net.URI;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class ScannedDocumentMapperTest {

    private final ScannedDocumentMapper mapper = new ScannedDocumentMapper();

    @Test
    public void toCCDScannedDocument() {
        LocalDateTime now = LocalDateTime.now();
        ScannedDocument scannedDocument = ScannedDocument.builder()
            .documentType(ScannedDocumentType.CHERISHED)
            .documentManagementUrl(URI.create("http://www.cnn.com/fake/news"))
            .documentManagementBinaryUrl(URI.create("http://www.cnn.com/fake/news/101"))
            .scannedDate(now)
            .deliveryDate(now)
            .controlNumber("Control")
            .exceptionRecordReference("exceptionRecordReference")
            .fileName("fileName")
            .id("id")
            .subtype("N9a")
            .build();

        CCDCollectionElement<CCDScannedDocument> ccdScannedDocumentElement = mapper.to(scannedDocument);
        CCDScannedDocument document = ccdScannedDocumentElement.getValue();
        CCDDocument documentUrl = document.getUrl();
        assertEquals(scannedDocument.getId(), ccdScannedDocumentElement.getId());
        assertEquals(CCDScannedDocumentType.cherished, document.getType());
        assertEquals(scannedDocument.getDocumentManagementBinaryUrl().toString(), documentUrl.getDocumentBinaryUrl());
        assertEquals(scannedDocument.getDocumentManagementUrl().toString(), documentUrl.getDocumentUrl());
        assertEquals(now, document.getScannedDate());
        assertEquals(now, document.getDeliveryDate());
        assertEquals(scannedDocument.getControlNumber(), document.getControlNumber());
        assertEquals(scannedDocument.getExceptionRecordReference(), document.getExceptionRecordReference());
        assertEquals(scannedDocument.getSubtype(), document.getSubtype());
        assertEquals(scannedDocument.getControlNumber(), document.getControlNumber());
    }

    @Test
    public void fromCCDScannedDocument() {
        LocalDateTime now = LocalDateTime.now();
        CCDDocument document = CCDDocument.builder().documentBinaryUrl("http://www.cnn.com")
            .documentFileName("fileName")
            .documentUrl("http://www.cnn.com").build();
        CCDScannedDocument ccdScannedDocument = CCDScannedDocument.builder()
            .type(CCDScannedDocumentType.cherished)
            .url(document)
            .scannedDate(now)
            .deliveryDate(now)
            .controlNumber("Control")
            .exceptionRecordReference("exceptionRecordReference")
            .fileName("fileName")
            .subtype("N9a")
            .build();
        CCDCollectionElement<CCDScannedDocument> collectionElement = CCDCollectionElement
            .<CCDScannedDocument>builder()
            .id("id")
            .value(ccdScannedDocument)
            .build();

        ScannedDocument scannedDocument = mapper.from(collectionElement);
        assertEquals(collectionElement.getId(), scannedDocument.getId());
        assertEquals(ScannedDocumentType.CHERISHED, scannedDocument.getDocumentType());
        assertEquals(document.getDocumentUrl(), scannedDocument.getDocumentManagementUrl().toString());
        assertEquals(document.getDocumentBinaryUrl(), scannedDocument.getDocumentManagementBinaryUrl().toString());
        assertEquals(now, scannedDocument.getDeliveryDate());
        assertEquals(now, scannedDocument.getScannedDate());
        assertEquals(scannedDocument.getControlNumber(), scannedDocument.getControlNumber());
        assertEquals(scannedDocument.getExceptionRecordReference(), scannedDocument.getExceptionRecordReference());
        assertEquals(scannedDocument.getSubtype(), scannedDocument.getSubtype());
        assertEquals(scannedDocument.getControlNumber(), scannedDocument.getControlNumber());
    }

}
