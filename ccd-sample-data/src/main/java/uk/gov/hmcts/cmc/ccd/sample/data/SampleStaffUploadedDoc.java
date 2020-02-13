package uk.gov.hmcts.cmc.ccd.sample.data;

import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocumentType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class SampleStaffUploadedDoc {

    private SampleStaffUploadedDoc() {
        // Utility private constructor
    }

    static final List<CCDCollectionElement<CCDClaimDocument>> staffUploadedDocs = Collections.singletonList(
        CCDCollectionElement.<CCDClaimDocument>builder()
            .id("2323-2342-34-2-342")
            .value(CCDClaimDocument.builder()
                .documentName("name")
                .documentLink(CCDDocument.builder()
                    .documentUrl("http://www.cnn.com")
                    .documentBinaryUrl("http://www.cnn.com")
                    .documentFileName("documentFileName").build()
                )
                .documentType(CCDClaimDocumentType.PAPER_RESPONSE_DISPUTES_ALL).build())
            .build());

    static final List<CCDCollectionElement<CCDClaimDocument>> staffUploadedDocsMediationAgreementPDF =
            Collections.singletonList(
            CCDCollectionElement.<CCDClaimDocument>builder()
                    .id("2323-2342-34-2-341")
                    .value(CCDClaimDocument.builder()
                            .documentName("name")
                            .documentLink(CCDDocument.builder()
                                    .documentUrl("http://www.cnn.com")
                                    .documentBinaryUrl("http://www.cnn.com")
                                    .documentFileName("documentFileName.pdf").build()
                            )
                            .documentType(CCDClaimDocumentType.MEDIATION_AGREEMENT).build())
                    .build());

    static final List<CCDCollectionElement<CCDClaimDocument>> staffUploadedDocsMediationAgreementNotPDF =
            Collections.singletonList(
            CCDCollectionElement.<CCDClaimDocument>builder()
                    .id("2323-2342-34-2-352")
                    .value(CCDClaimDocument.builder()
                            .documentName("name")
                            .documentLink(CCDDocument.builder()
                                    .documentUrl("http://www.cnn.com")
                                    .documentBinaryUrl("http://www.cnn.com")
                                    .documentFileName("documentFileName").build()
                            )
                            .documentType(CCDClaimDocumentType.MEDIATION_AGREEMENT).build())
                    .build());

    static final List<CCDCollectionElement<CCDClaimDocument>> StaffDocsMediationPDFSecond = Arrays.asList(
            CCDCollectionElement.<CCDClaimDocument>builder()
                    .id("2323-2342-34-2-352")
                    .value(CCDClaimDocument.builder()
                            .documentName("name")
                            .documentLink(CCDDocument.builder()
                                    .documentUrl("http://www.cnn.com")
                                    .documentBinaryUrl("http://www.cnn.com")
                                    .documentFileName("documentFileName").build()
                            )
                            .documentType(CCDClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT).build())
                    .build(),
            CCDCollectionElement.<CCDClaimDocument>builder()
                    .id("2323-2342-34-2-353")
                    .value(CCDClaimDocument.builder()
                            .documentName("name")
                            .documentLink(CCDDocument.builder()
                                    .documentUrl("http://www.cnn.com")
                                    .documentBinaryUrl("http://www.cnn.com")
                                    .documentFileName("documentFileName.pdf").build()
                            )
                            .documentType(CCDClaimDocumentType.MEDIATION_AGREEMENT).build())
                    .build());

    static final List<CCDCollectionElement<CCDClaimDocument>> StaffDocPDFMediationSecond = Arrays.asList(
            CCDCollectionElement.<CCDClaimDocument>builder()
                    .id("2323-2342-34-2-352")
                    .value(CCDClaimDocument.builder()
                            .documentName("name")
                            .documentLink(CCDDocument.builder()
                                    .documentUrl("http://www.cnn.com")
                                    .documentBinaryUrl("http://www.cnn.com")
                                    .documentFileName("documentFileName.pdf").build()
                            )
                            .documentType(CCDClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT).build())
                    .build(),
            CCDCollectionElement.<CCDClaimDocument>builder()
                    .id("2323-2342-34-2-353")
                    .value(CCDClaimDocument.builder()
                            .documentName("name")
                            .documentLink(CCDDocument.builder()
                                    .documentUrl("http://www.cnn.com")
                                    .documentBinaryUrl("http://www.cnn.com")
                                    .documentFileName("documentFileName").build()
                            )
                            .documentType(CCDClaimDocumentType.MEDIATION_AGREEMENT).build())
                    .build());

    static final List<CCDCollectionElement<CCDClaimDocument>> MediationPDFFirstStaffDocPDFSecond = Arrays.asList(
            CCDCollectionElement.<CCDClaimDocument>builder()
                    .id("2323-2342-34-2-352")
                    .value(CCDClaimDocument.builder()
                            .documentName("name")
                            .documentLink(CCDDocument.builder()
                                    .documentUrl("http://www.cnn.com")
                                    .documentBinaryUrl("http://www.cnn.com")
                                    .documentFileName("documentFileName.pdf").build()
                            )
                            .documentType(CCDClaimDocumentType.MEDIATION_AGREEMENT).build())
                    .build(),
            CCDCollectionElement.<CCDClaimDocument>builder()
                    .id("2323-2342-34-2-353")
                    .value(CCDClaimDocument.builder()
                            .documentName("name")
                            .documentLink(CCDDocument.builder()
                                    .documentUrl("http://www.cnn.com")
                                    .documentBinaryUrl("http://www.cnn.com")
                                    .documentFileName("documentFileName.pdf").build()
                            )
                            .documentType(CCDClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT).build())
                    .build());

    static final List<CCDCollectionElement<CCDScannedDocument>> scannedDocsPaperResponse = Collections.singletonList(
        CCDCollectionElement.<CCDScannedDocument>builder()
            .id("2323-2342-34-2-342")
            .value(CCDScannedDocument.builder()
                .fileName("name")
                .type(CCDScannedDocumentType.cherished)
                .scannedDate(LocalDateTime.now())
                .exceptionRecordReference("reference")
                .deliveryDate(LocalDateTime.now())
                .url(CCDDocument.builder()
                    .documentUrl("http://www.cnn.com")
                    .documentBinaryUrl("http://www.cnn.com")
                    .documentFileName("fileName")
                    .build())
                .controlNumber("controlNumber")
                .subtype("N225").build())
            .build());

}
