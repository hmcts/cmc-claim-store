package uk.gov.hmcts.cmc.claimstore.events.claim;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
@Builder
public class GeneratedDocuments {
    private PDF claimIssueReceipt;
    private PDF defendantPinLetter;
    private PDF sealedClaim;
    private Document defendantPinLetterDoc;
    private Document sealedClaimDoc;
    private String pin;
    private final Claim claim;

    public GeneratedDocuments(
        PDF claimIssueReceipt,
        PDF defendantPinLetter,
        PDF sealedClaim,
        Document defendantPinLetterDoc,
        Document sealedClaimDoc,
        String pin,
        Claim claim
    ) {
        this.claimIssueReceipt = claimIssueReceipt;
        this.defendantPinLetter = defendantPinLetter;
        this.sealedClaim = sealedClaim;
        this.defendantPinLetterDoc = defendantPinLetterDoc;
        this.sealedClaimDoc = sealedClaimDoc;
        this.pin = pin;
        this.claim = claim;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
