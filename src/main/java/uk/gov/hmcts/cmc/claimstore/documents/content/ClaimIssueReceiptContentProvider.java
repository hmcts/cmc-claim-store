package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

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

        result.put("fullName", "full name def");
        result.put("businessName", "bus name def");
        result.put("address", createContentForAddress(claim.getClaimData().getDefendant().getAddress()));
        result.put("email", "def email");

        return result;

    }

    private Map<String, Object> createContentForClaim(final Claim claim) {
        Map<String, Object> result = new HashMap<>();

        result.put("claimReferenceNumber", "000MC999");
        result.put("claimantFullname", "Claimie McClaimant");
        result.put("submittedDate", "27 November 2017 at 4:16pm");

        result.put("amount", "2844");
        result.put("interest", createContentForInterest(claim.getClaimData().getInterest()));
        result.put("issueFee", "999");
        result.put("totalAmountTillDateOfIssue", "the total");
        result.put("reason", "the reason");
        result.put("statementOfTruth", createContentForStatementOfTruth(claim.getClaimData().getStatementOfTruth()));

        return result;
    }

    private Map<String, Object> createContentForClaimant(final Claim claim) {
        Map<String, Object> result = new HashMap<>();

        result.put("fullName", "full name");
        result.put("contactPerson", "contact name ");
        result.put("businessName", "Business Name");
        result.put("address", createContentForAddress(claim.getClaimData().getClaimant().getAddress()));
        result.put("correspondenceAddress", createContentForCorrespondenceAddress(claim.getClaimData().getClaimant().getCorrespondenceAddress()));
        result.put("email", "emailAddress");

        return result;

    }

    private Map<String, Object> createContentForStatementOfTruth(Optional<StatementOfTruth> statementOfTruth) {
        Map<String, Object> result = new HashMap<>();


        result.put("signerName", statementOfTruth.map((StatementOfTruth::getSignerName)).orElse(null));
        result.put("signerRole", statementOfTruth.map((StatementOfTruth::getSignerRole)).orElse(null));

        return result;
    }

    private Map<String, Object> createContentForInterest(Interest interest) {
        Map<String, Object> result = new HashMap<>();

        result.put("rate", "3");
        result.put("dateClaimedFrom", "4");
        result.put("accruedInterest", "5");
        result.put("dateClaimedFrom", "6");
        result.put("claimedAtDateOfSubmission", "7");

        return result;
    }

    private Map<String, Object> createContentForAddress(Address address) {
        Map<String, Object> result = new HashMap<>();

        result.put("line1", "1 Address Lane");
        result.put("line2", "Street Road");
        result.put("city", "That City");
        result.put("postcode", "SW1H 3LL");

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
