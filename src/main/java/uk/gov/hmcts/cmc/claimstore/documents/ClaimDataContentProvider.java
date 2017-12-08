package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class ClaimDataContentProvider {

    private final InterestContentProvider interestContentProvider;

    @Autowired
    public ClaimDataContentProvider(InterestContentProvider interestContentProvider) {
        this.interestContentProvider = interestContentProvider;
    }

    public ClaimContent createContent(Claim claim) {
        requireNonNull(claim);

        List<BigDecimal> totalAmountComponents = new ArrayList<>();
        totalAmountComponents.add(((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount());
        totalAmountComponents.add(claim.getClaimData().getPayment().getAmountInPounds());

        InterestContent interestContent = null;
        if (!claim.getClaimData().getInterest().getType().equals(Interest.InterestType.NO_INTEREST)) {
            interestContent = interestContentProvider.createContent(
                claim.getClaimData().getInterest(),
                claim.getClaimData().getInterestDate(),
                ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount(),
                claim.getCreatedAt()
            );
            totalAmountComponents.add(interestContent.getAmountRealValue());
        }

        Optional<StatementOfTruth> optionalStatementOfTruth = claim.getClaimData().getStatementOfTruth();
        String signerName = optionalStatementOfTruth.map((StatementOfTruth::getSignerName)).orElse(null);
        String signerRole = optionalStatementOfTruth.map((StatementOfTruth::getSignerRole)).orElse(null);

        return new ClaimContent(
            claim.getReferenceNumber(),
            formatDateTime(claim.getCreatedAt()),
            formatDate(claim.getIssuedOn()),
            claim.getClaimData().getReason(),
            formatMoney(((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount()),
            formatMoney(claim.getClaimData().getPayment().getAmountInPounds()),
            interestContent,
            formatMoney(
                totalAmountComponents.stream()
                    .filter(Objects::nonNull)
                    .reduce(ZERO, BigDecimal::add)
            ),
            signerName,
            signerRole
        );
    }

}
