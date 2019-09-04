package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContent;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.services.staff.content.RepaymentPlanContentProvider.create;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class CCJContent {

    private final Map<String, Object> claim;
    private final String requestedAt;
    private final String requestedDate;
    private final AmountContent amount;
    private final String defendantDateOfBirth;
    private final RepaymentPlanContent repaymentPlan;
    private final String signerName;
    private final String signerRole;
    private final String responseType;
    private final String ccjType;

    public CCJContent(
        Map<String, Object> claim,
        CountyCourtJudgment countyCourtJudgment,
        LocalDateTime countyCourtJudgmentRequestedAt,
        AmountContent amount,
        String responseType
    ) {
        requireNonNull(claim);
        requireNonNull(countyCourtJudgment);
        requireNonNull(countyCourtJudgmentRequestedAt);
        requireNonNull(amount);

        this.claim = claim;
        this.amount = amount;
        this.defendantDateOfBirth = countyCourtJudgment.getDefendantDateOfBirth()
            .map(Formatting::formatDate).orElse(null);
        this.repaymentPlan = create(countyCourtJudgment.getPaymentOption(),
            countyCourtJudgment.getRepaymentPlan().orElse(null),
            countyCourtJudgment.getPayBySetDate().orElse(null)
        );
        this.requestedAt = Formatting.formatDateTime(countyCourtJudgmentRequestedAt);
        this.requestedDate = formatDate(countyCourtJudgmentRequestedAt);

        Optional<StatementOfTruth> optionalStatementOfTruth = countyCourtJudgment.getStatementOfTruth();
        this.signerName = optionalStatementOfTruth.map((StatementOfTruth::getSignerName)).orElse(null);
        this.signerRole = optionalStatementOfTruth.map((StatementOfTruth::getSignerRole)).orElse(null);
        this.responseType = responseType;
        this.ccjType = countyCourtJudgment.getCcjType().name();
    }

    public Map<String, Object> getClaim() {
        return claim;
    }

    public AmountContent getAmount() {
        return amount;
    }

    public String getDefendantDateOfBirth() {
        return defendantDateOfBirth;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public RepaymentPlanContent getRepaymentPlan() {
        return repaymentPlan;
    }

    public String getSignerName() {
        return signerName;
    }

    public String getSignerRole() {
        return signerRole;
    }

    public String getResponseType() {
        return responseType;
    }

    public String getCcjType() {
        return ccjType;
    }
}
