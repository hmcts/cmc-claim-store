package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import uk.gov.hmcts.cmc.domain.models.AmountRow;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public class AmountRowContent {
    private final String reason;
    private final String amount;

    public AmountRowContent(AmountRow amountRow) {
        this.reason = amountRow.getReason();
        this.amount = formatMoney(amountRow.getAmount());
    }

    public String getReason() {
        return reason;
    }

    public String getAmount() {
        return amount;
    }
}
