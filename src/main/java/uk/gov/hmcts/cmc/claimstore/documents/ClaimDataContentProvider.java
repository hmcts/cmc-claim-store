package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.EvidenceContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.AmountRowContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
        AmountBreakDown amountBreakDown = (AmountBreakDown) claim.getClaimData().getAmount();
        totalAmountComponents.add(amountBreakDown.getTotalAmount());
        totalAmountComponents.add(claim.getClaimData().getFeesPaidInPound());

        InterestContent interestContent = null;
        if (!claim.getClaimData().getInterest().getType().equals(Interest.InterestType.NO_INTEREST)) {
            interestContent = interestContentProvider.createContent(
                claim.getClaimData().getInterest(),
                claim.getClaimData().getInterest().getInterestDate(),
                amountBreakDown.getTotalAmount(),
                claim.getIssuedOn(),
                claim.getIssuedOn()
            );

            totalAmountComponents.add(interestContent.getAmountRealValue());
        }

        Optional<StatementOfTruth> optionalStatementOfTruth = claim.getClaimData().getStatementOfTruth();
        String signerName = optionalStatementOfTruth.map((StatementOfTruth::getSignerName)).orElse(null);
        String signerRole = optionalStatementOfTruth.map((StatementOfTruth::getSignerRole)).orElse(null);

        List<TimelineEvent> events = null;
        Optional<Timeline> timeline = claim.getClaimData().getTimeline();
        if (timeline.isPresent()) {
            events = timeline.get().getEvents();
        }

        List<EvidenceContent> evidences = null;
        Optional<Evidence> evidence = claim.getClaimData().getEvidence();
        if (evidence.isPresent()) {
            evidences = Optional.ofNullable(evidence.get().getRows())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(Objects::nonNull)
                .map(e -> new EvidenceContent(e.getType().getDescription(), e.getDescription().orElse(null)))
                .collect(Collectors.toList());
        }

        return new ClaimContent(
            claim.getReferenceNumber(),
            formatDateTime(claim.getCreatedAt()),
            formatDate(claim.getIssuedOn()),
            claim.getClaimData().getReason(),
            formatMoney(amountBreakDown.getTotalAmount()),
            formatMoney(claim.getClaimData().getFeesPaidInPound()),
            interestContent,
            formatMoney(
                totalAmountComponents.stream()
                    .filter(Objects::nonNull)
                    .reduce(ZERO, BigDecimal::add)
            ),
            signerName,
            signerRole,
            events,
            evidences,
            mapToAmountRowContent(amountBreakDown.getRows())
        );
    }

    private static List<AmountRowContent> mapToAmountRowContent(List<AmountRow> rows) {
        return rows
            .stream()
            .filter(row -> row != null && row.getAmount() != null)
            .map(AmountRowContent::new)
            .collect(Collectors.toList());
    }
}
