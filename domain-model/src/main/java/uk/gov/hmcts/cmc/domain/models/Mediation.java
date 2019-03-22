package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class Mediation {
    @Builder.Default
    private int siteId = 4;
    @Builder.Default
    private final int caseType = 1;
    @Builder.Default
    private final int checkList = 4;
    @Builder.Default
    private int partyStatus = 5;

    private String caseNumber;
    private BigDecimal amount;
    private int partyType;
    private String contactName;
    private String contactNumber;
}
