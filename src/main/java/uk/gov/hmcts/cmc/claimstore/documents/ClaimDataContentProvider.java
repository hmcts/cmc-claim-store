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
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.claimstore.utils.ParagraphEnumerator.split;

@Component
public class ClaimDataContentProvider {

    private static final Function<EvidenceRow, EvidenceContent> toEvidenceContent =
        e -> new EvidenceContent(e.getType().getDescription(), e.getDescription().orElse(null));

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
        totalAmountComponents.add(claim.getClaimData().getFeesPaidInPounds().orElse(ZERO));

        InterestContent interestContent = null;

        if (!Objects.equals(claim.getClaimData().getInterest().getType(), Interest.InterestType.NO_INTEREST)) {
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

        List<TimelineEvent> events = claim.getClaimData().getTimeline().map(Timeline::getEvents).orElse(null);

        List<EvidenceContent> evidences = claim.getClaimData().getEvidence()
            .map(Evidence::getRows)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(Objects::nonNull)
            .map(toEvidenceContent)
            .collect(Collectors.toList());

        return new ClaimContent(
            claim.getReferenceNumber(),
            formatDateTime(claim.getCreatedAt()),
            formatDate(claim.getIssuedOn()),
            split(claim.getClaimData().getReason()),
            formatMoney(amountBreakDown.getTotalAmount()),
            formatMoney(claim.getClaimData().getFeesPaidInPounds().orElse(ZERO)),
            interestContent,
            formatMoney(
                totalAmountComponents.stream()
                    .filter(Objects::nonNull)
                    .reduce(ZERO, BigDecimal::add)
            ),
            events,
            evidences,
            mapToAmountRowContent(amountBreakDown.getRows()),
            optionalStatementOfTruth.orElse(null)
        );
    }

    private static List<AmountRowContent> mapToAmountRowContent(List<AmountRow> rows) {
        return rows
            .stream()
            .filter(Objects::nonNull)
            .filter(row -> row.getAmount() != null)
            .map(AmountRowContent::new)
            .collect(Collectors.toList());
    }
}
