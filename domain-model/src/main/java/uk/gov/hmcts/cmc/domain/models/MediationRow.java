package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@EqualsAndHashCode
public class MediationRow implements Iterable<String> {
    private String siteId;
    private String caseNumber;
    private String caseType;
    private String amount;
    private String partyType;
    private String contactName;
    private String contactEmailAddress;
    private String contactNumber;
    private String checkList;
    private String partyStatus;

    @Builder
    public MediationRow(
        String siteId,
        String caseNumber,
        String caseType,
        String amount,
        String partyType,
        String contactName,
        String contactEmailAddress,
        String contactNumber,
        String checkList,
        String partyStatus
    ) {
        this.siteId = siteId;
        this.caseNumber = caseNumber;
        this.caseType = caseType;
        this.amount = amount;
        this.partyType = partyType;
        this.contactName = contactName;
        this.contactEmailAddress = contactEmailAddress;
        this.contactNumber = contactNumber;
        this.checkList = checkList;
        this.partyStatus = partyStatus;
    }

    public List<String> toList() {
        return Arrays.asList(
            siteId,
            caseNumber,
            caseType,
            amount,
            partyType,
            contactName,
            contactEmailAddress,
            contactNumber,
            checkList,
            partyStatus
        );
    }

    @Override
    public Iterator<String> iterator() {
        return this.toList().iterator();
    }
}
