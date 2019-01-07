package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Value
public class CCDPaymentIntention {
    private CCDPaymentOption paymentOption;
    private LocalDate paymentDate;
    private BigDecimal instalmentAmount;
    private LocalDate firstPaymentDate;
    private CCDPaymentSchedule paymentSchedule;
    private LocalDate completionDate;
    private String paymentLength;

    @JsonIgnore
    public boolean hasRepaymentPlanInfo() {
        return !(null == instalmentAmount
            || null == firstPaymentDate
            || null == paymentSchedule
            || null == completionDate
            || StringUtils.isBlank(paymentLength));
    }
}
