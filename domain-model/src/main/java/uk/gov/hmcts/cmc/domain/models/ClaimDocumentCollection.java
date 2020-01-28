package uk.gov.hmcts.cmc.domain.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Getter
public class ClaimDocumentCollection {

    private final List<ClaimDocument> claimDocuments = new ArrayList<>();

    private final List<ScannedDocument> scannedDocuments = new ArrayList<>();

    private final List<ClaimDocument> staffUploadedDocuments = new ArrayList<>();

    public void addClaimDocument(ClaimDocument claimDocument) {
        claimDocuments.add(claimDocument);
    }

    public void addScannedDocument(ScannedDocument scannedDocument) {
        scannedDocuments.add(scannedDocument);
    }

    public void addStaffUploadedDocument(ClaimDocument claimDocument) {
        staffUploadedDocuments.add(claimDocument);
    }

    public Optional<ClaimDocument> getDocument(ClaimDocumentType claimDocumentType) {
        return Stream.concat(claimDocuments.stream(), staffUploadedDocuments.stream())
            .filter(claimDocument -> claimDocument.getDocumentType().equals(claimDocumentType))
            .findFirst();
    }
}
