package uk.gov.hmcts.cmc.domain.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class ClaimDocumentStore {

    private final List<ClaimDocument> claimDocuments = new ArrayList<ClaimDocument>();

    public void addClaimDocument(ClaimDocument claimDocument) {
        claimDocuments.add(claimDocument);
    }

    public Optional<ClaimDocument> getDocument(ClaimDocumentType claimDocumentType) {
        return claimDocuments.stream()
            .filter(claimDocument -> claimDocument.getDocumentType().equals(claimDocumentType))
            .findFirst();
    }
}
