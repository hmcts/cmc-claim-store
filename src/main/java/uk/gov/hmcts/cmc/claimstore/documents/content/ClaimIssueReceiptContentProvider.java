package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class ClaimIssueReceiptContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimContentProvider claimContentProvider;

    public ClaimIssueReceiptContentProvider(
        final PartyDetailsContentProvider partyDetailsContentProvider,
        final ClaimContentProvider claimContentProvider
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
        this.claimContentProvider = claimContentProvider;
    }

    public Map<String, Object> createContent(final Claim claim) {
        requireNonNull(claim);

        Map<String, Object> content = new HashMap<>();
        content.put("claimReferenceNumber", claim.getReferenceNumber());
        content.put("claimSubmittedOn", formatDate(claim.getCreatedAt()));

        content.put("claimant", createContentForClaimant(claim));
        content.put("defendant", createContentForDefendant(claim));
        content.put("claim", createContentForClaim(claim));

        return content;
    }


    private Map<String, Object> createContentForDefendant(final Claim claim) {
        Map<String, Object> result = new HashMap<>();

        result.put("fullName", claim.getClaimData().getDefendant().getName());
        result.put("businessName", PartyUtils.getBusinessName(claim.getClaimData().getDefendant()));
        result.put("address", createContentForAddress(claim.getClaimData().getDefendant().getAddress()));
        result.put("email", claim.getClaimData().getDefendant().getEmail());

        return result;

    }

    private Map<String, Object> createContentForClaim(final Claim claim) {
        Map<String, Object> result = new HashMap<>();

        result.put("claimReferenceNumber", claim.getReferenceNumber());
        result.put("submittedDate", formatDate(claim.getCreatedAt()));

        result.put("amount", claim.getClaimData().getAmount()); //TODO Talk to damian. Getting JSON BACK {rows=[{reason=Food, amount=1234}, {}, {}, {}]}
        result.put("interest", createContentForInterest(claim.getClaimData().getInterest()));
        result.put("issueFee", claim.getClaimData().getFeesPaidInPound()); ///////////////////Todo
        result.put("totalAmountTillDateOfIssue", claim.getClaimData().getFeeAmountInPennies()); ///////////////////Todo
        result.put("reason", claim.getClaimData().getReason());
        result.put("statementOfTruth", createContentForStatementOfTruth(claim.getClaimData()));

        return result;
    }

    private Map<String, Object> createContentForClaimant(final Claim claim) {
        Map<String, Object> result = new HashMap<>();

        result.put("fullName", claim.getClaimData().getClaimant().getName());
        result.put("contactPerson", PartyUtils.getContactPerson(claim.getClaimData().getClaimant()));
        result.put("businessName", PartyUtils.getBusinessName(claim.getClaimData().getClaimant()));
        result.put("address", createContentForAddress(claim.getClaimData().getClaimant().getAddress()));
        result.put("correspondenceAddress", claim.getClaimData().getClaimant().getCorrespondenceAddress());
        result.put("email", claim.getSubmitterEmail()); //todo Get email for claimant

        return result;

    }

    private Map<String, Object> createContentForStatementOfTruth(ClaimData claimData) {
        Map<String, Object> result = null;

        if (claimData.getStatementOfTruth().isPresent()) {
            result = new HashMap<>();

            StatementOfTruth statementOfTruth = claimData.getStatementOfTruth().get();
            result.put("signerName", statementOfTruth.getSignerName());
            result.put("signerRole", statementOfTruth.getSignerRole());
        }
        return result;
    }

    private Map<String, Object> createContentForInterest(Interest interest) {
        Map<String, Object> result = new HashMap<>();

        result.put("rate", interest.getRate());
        result.put("dateClaimedFrom", interest.getType());
        result.put("accruedInterest", "5");
        result.put("dateClaimedFrom", "6");
        result.put("claimedAtDateOfSubmission", "7");

        return result;
    }

    private Map<String, Object> createContentForAddress(Address address) {
        Map<String, Object> result = new HashMap<>();

        result.put("line1", address.getLine1());
        result.put("line2", address.getLine2());
        result.put("city", address.getCity());
        result.put("postcode", address.getPostcode());

        return result;
    }

    private Map<String, Object> createContentForCorrespondenceAddress(Optional<Address> address) {
        Map<String, Object> result = new HashMap<>();

        result.put("line1", "999");
        result.put("line2", "888");
        result.put("city", "777");
        result.put("postcode", "TW16 5EH");

        return result;
    }

}
