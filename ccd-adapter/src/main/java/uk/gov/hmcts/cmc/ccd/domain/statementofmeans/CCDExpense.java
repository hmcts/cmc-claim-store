package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

import java.math.BigDecimal;

@Value
@Builder
public class CCDExpense {
    private String type;
    private PaymentFrequency frequency;
    private BigDecimal amountPaid;
}
