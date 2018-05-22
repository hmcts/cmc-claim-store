package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidence;

import java.math.BigInteger;
import java.util.List;

@Value
@Builder
public class CCDClaim {

    private String reason;
    private String feeCode;
    private String feeAccountNumber;
    private BigInteger feeAmountInPennies;
    private String externalReferenceNumber;
    private String externalId;
    private CCDAmount amount;
    private CCDInterest interest;
    private CCDPayment payment;
    private String preferredCourt;
    private CCDPersonalInjury personalInjury;
    private CCDHousingDisrepair housingDisrepair;
    private CCDStatementOfTruth statementOfTruth;
    private List<CCDCollectionElement<CCDParty>> claimants;
    private List<CCDCollectionElement<CCDParty>> defendants;
    private CCDTimeline timeline;
    private CCDEvidence evidence;
}
