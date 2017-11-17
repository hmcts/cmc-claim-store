package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CCDClaim {

    private final String reason;
    private final String feeCode;
    private final String feeAccountNumber;
    private final BigDecimal feeAmountInPennies;
    private final String externalReferenceNumber;
    private final String externalId;
    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;
    private final String preferredCourt;
    private final CCDPersonalInjury personalInjury;
    private final CCDHousingDisrepair housingDisrepair;
    private final CCDStatementOfTruth statementOfTruth;
    private final List<CCDParty> claimants;
    private final List<CCDParty> defendants;
}

