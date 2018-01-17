package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class CCDClaim {

    private String reason;
    private String feeCode;
    private String feeAccountNumber;
    private BigInteger feeAmountInPennies;
    private String externalReferenceNumber;
    private String externalId;
    private CCDAmount amount;
    private CCDInterest interest;
    private CCDInterestDate interestDate;
    private CCDPayment payment;
    private String preferredCourt;
    private CCDPersonalInjury personalInjury;
    private CCDHousingDisrepair housingDisrepair;
    private CCDStatementOfTruth statementOfTruth;
    private List<Map<String, CCDParty>> claimants;
    private List<Map<String, CCDParty>> defendants;
}

