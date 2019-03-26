package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;

import java.util.Arrays;
import java.util.List;

@Builder
public class MediationRow {
    @Builder.Default
    private String siteId = "4";
    @Builder.Default
    private String caseType = "1";
    @Builder.Default
    private String checkList = "4";
    @Builder.Default
    private String partyStatus = "5";

    private String caseNumber;
    private String amount;
    private String partyType;
    private String contactName;
    private String contactNumber;


    public List<String> toArray() {
        return Arrays.asList(
            siteId,
            caseType,
            checkList,
            partyStatus,
            caseNumber,
            amount,
            partyType,
            contactName,
            contactNumber
        );
    }
}
