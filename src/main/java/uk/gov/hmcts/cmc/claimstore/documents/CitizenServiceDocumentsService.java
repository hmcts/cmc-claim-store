package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantPinLetterContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

@Service
public class CitizenServiceDocumentsService {

    private final DocumentTemplates documentTemplates;
    private final ClaimContentProvider claimContentProvider;
    private final DefendantPinLetterContentProvider letterContentProvider;

    @Autowired
    public CitizenServiceDocumentsService(
        DocumentTemplates documentTemplates,
        ClaimContentProvider claimContentProvider,
        DefendantPinLetterContentProvider letterContentProvider
    ) {
        this.documentTemplates = documentTemplates;
        this.claimContentProvider = claimContentProvider;
        this.letterContentProvider = letterContentProvider;
    }

    public Document sealedClaimDocument(Claim claim) {
        return new Document(new String(documentTemplates.getSealedClaim()), claimContentProvider.createContent(claim));
    }

    public Document pinLetterDocument(Claim claim, String defendantPin) {
        return new Document(
            new String(documentTemplates.getDefendantPinLetter()),
            letterContentProvider.createContent(claim, defendantPin)
        );
    }
}
