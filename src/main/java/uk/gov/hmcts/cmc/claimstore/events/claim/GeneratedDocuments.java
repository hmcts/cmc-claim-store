package uk.gov.hmcts.cmc.claimstore.events.claim;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
@Builder
public class GeneratedDocuments {
    private PDF claimIssueReceipt;
    private PDF defendantLetter;
    private PDF sealedClaim;
    private Document defendantLetterDoc;
    private Document sealedClaimDoc;
    private String pin;

    public GeneratedDocuments(
        PDF claimIssueReceipt,
        PDF defendantLetter,
        PDF sealedClaim,
        Document defendantLetterDoc,
        Document sealedClaimDoc,
        String pin
    ) {
        this.claimIssueReceipt = claimIssueReceipt;
        this.defendantLetter = defendantLetter;
        this.sealedClaim = sealedClaim;
        this.defendantLetterDoc = defendantLetterDoc;
        this.sealedClaimDoc = sealedClaimDoc;
        this.pin = pin;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
