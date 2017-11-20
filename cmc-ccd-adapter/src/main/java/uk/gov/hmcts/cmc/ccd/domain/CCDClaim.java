package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Value
@Builder
public class CCDClaim {

    private  String reason;
    private  String feeCode;
    private  String feeAccountNumber;
    private  BigInteger feeAmountInPennies;
    private  String externalReferenceNumber;
    private  String externalId;
    private  BigDecimal minAmount;
    private  BigDecimal maxAmount;
    private  String preferredCourt;
    private  CCDPersonalInjury personalInjury;
    private  CCDHousingDisrepair housingDisrepair;
    private  CCDStatementOfTruth statementOfTruth;
    private  List<CCDParty> claimants;
    private  List<CCDParty> defendants;
}

