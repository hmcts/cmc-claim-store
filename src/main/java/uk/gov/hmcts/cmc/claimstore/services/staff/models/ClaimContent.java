package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.documents.content.models.EvidenceContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.AmountRowContent;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.util.List;

public class ClaimContent {

    private final String referenceNumber;
    private final String submittedOn;
    private final String issuedOn;
    private final List<String> reason;
    private final String claimAmount;
    private final String feeAmount;
    private final InterestContent interest;
    private final String claimTotalAmount;
    private final List<TimelineEvent> events;
    private final List<EvidenceContent> evidences;
    private final List<AmountRowContent> amountBreakdown;
    private final StatementOfTruth statementOfTruth;

    @SuppressWarnings("squid:S00107") // Suppressed due to MVP timelines, require more time to investigate and fix
    public ClaimContent(
            String referenceNumber,
            String submittedOn,
            String issuedOn,
            List<String> reason,
            String claimAmount,
            String feeAmount,
            InterestContent interest,
            String claimTotalAmount,
            List<TimelineEvent> events,
            List<EvidenceContent> evidences,
            List<AmountRowContent> amountBreakdown,
            StatementOfTruth statementOfTruth
    ) {
        this.referenceNumber = referenceNumber;
        this.submittedOn = submittedOn;
        this.issuedOn = issuedOn;
        this.reason = reason;
        this.claimAmount = claimAmount;
        this.feeAmount = feeAmount;
        this.interest = interest;
        this.claimTotalAmount = claimTotalAmount;
        this.events = events;
        this.evidences = evidences;
        this.amountBreakdown = amountBreakdown;
        this.statementOfTruth = statementOfTruth;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getSubmittedOn() {
        return submittedOn;
    }

    public String getIssuedOn() {
        return issuedOn;
    }

    public List<String> getReason() {
        return reason;
    }

    public String getClaimAmount() {
        return claimAmount;
    }

    public String getFeeAmount() {
        return feeAmount;
    }

    public InterestContent getInterest() {
        return interest;
    }

    public String getClaimTotalAmount() {
        return claimTotalAmount;
    }

    public List<TimelineEvent> getEvents() {
        return events;
    }

    public List<EvidenceContent> getEvidences() {
        return evidences;
    }

    public List<AmountRowContent> getAmountBreakdown() {
        return amountBreakdown;
    }

    public StatementOfTruth getStatementOfTruth() {
        return statementOfTruth;
    }
}
