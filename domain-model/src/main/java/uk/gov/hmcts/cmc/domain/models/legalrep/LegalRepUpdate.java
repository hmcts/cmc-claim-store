package uk.gov.hmcts.cmc.domain.models.legalrep;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigInteger;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@Data
@EqualsAndHashCode
public class LegalRepUpdate {

    private final String externalId;
    private final String ccdCaseId;
    private final BigInteger feeAmountInPennies;
    private final String feeCode;
    private final PaymentReference paymentReference;
    private final String feeAccount;

    public LegalRepUpdate(String externalId,
                          String ccdCaseId,
                          BigInteger feeAmountInPennies,
                          String feeCode,
                          PaymentReference paymentReference,
                          String feeAccount) {
        this.externalId = externalId;
        this.ccdCaseId = ccdCaseId;
        this.feeAmountInPennies = feeAmountInPennies;
        this.feeCode = feeCode;
        this.paymentReference = paymentReference;
        this.feeAccount = feeAccount;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
