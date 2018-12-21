package uk.gov.hmcts.cmc.ccd.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Value
public class CCDPaymentIntention {
    private PaymentOption paymentOption;
    private LocalDate paymentDate;
    private BigDecimal instalmentAmount;
    private LocalDate firstPaymentDate;
    private PaymentSchedule paymentSchedule;
    private LocalDate completionDate;
    private String paymentLength;

    @JsonIgnore
    public boolean hasRepaymentPlanInfo() {
        return instalmentAmount != null
            && firstPaymentDate != null
            && paymentSchedule != null;
    }
}
