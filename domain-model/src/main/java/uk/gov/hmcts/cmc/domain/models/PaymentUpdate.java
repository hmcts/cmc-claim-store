package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;
import java.util.List;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PaymentUpdate {
    private final String id;
    /**
     * The amount which was paid, in pennies for payments v1 or pounds with payments v2.
     */
    private final BigDecimal amount;
    private final String description;
    private final String reference;
    private final String currency;
    private final String ccdCaseNumber;
    private final String caseReference;
    private final String channel;
    private final String method;
    private final String externalProvider;
    private final String status;
    private final String externalReference;
    private final String siteId;
    private final String serviceName;
    private final String paymentGroupReference;
    private final List<Fees> fees;

    @SuppressWarnings("squid:S00107")
    @Builder(toBuilder = true)
    public PaymentUpdate(String id,
                         BigDecimal amount,
                         String description,
                         String reference,
                         String currency,
                         String ccdCaseNumber,
                         String caseReference,
                         String channel,
                         String method,
                         String externalProvider,
                         String status,
                         String externalReference,
                         String siteId,
                         String serviceName,
                         String paymentGroupReference,
                         List<Fees> fees) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.reference = reference;
        this.currency = currency;
        this.ccdCaseNumber = ccdCaseNumber;
        this.caseReference = caseReference;
        this.channel = channel;
        this.method = method;
        this.externalProvider = externalProvider;
        this.status = status;
        this.externalReference = externalReference;
        this.siteId = siteId;
        this.serviceName = serviceName;
        this.paymentGroupReference = paymentGroupReference;
        this.fees = fees;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
