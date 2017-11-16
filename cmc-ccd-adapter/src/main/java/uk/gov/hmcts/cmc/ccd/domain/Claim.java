package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class Claim {

    private final String reason;
    private final String feeCode;
    private final String feeAccountNumber;
    private final BigDecimal feeAmountInPennies;
    private final String externalReferenceNumber;
    private final String externalId;
    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;
    private final String preferredCourt;
    private final PersonalInjury personalInjury;
    private final HousingDisrepair housingDisrepair;
    private final StatementOfTruth statementOfTruth;
    private final List<Party> claimants;
    private final List<Party> defendants;
}

