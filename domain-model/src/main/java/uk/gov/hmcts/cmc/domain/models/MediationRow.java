package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@EqualsAndHashCode
public class MediationRow implements Iterable<String> {
    public static final String SITE_ID = "4";
    public static final String CASE_TYPE = "1";
    public static final String CHECK_LIST = "4";
    public static final String PARTY_STATUS = "5";

    private String siteId;
    private String caseType;
    private String checkList;
    private String partyStatus;
    private String caseNumber;
    private String amount;
    private String partyType;
    private String contactName;
    private String contactNumber;
    private String contactEmailAddress;

    @Builder
    public MediationRow(
        String siteId,
        String caseType,
        String checkList,
        String partyStatus,
        String caseNumber,
        String amount,
        String partyType,
        String contactName,
        String contactNumber,
        String contactEmailAddress
    ) {
        this.siteId = siteId;
        this.caseType = caseType;
        this.checkList = checkList;
        this.partyStatus = partyStatus;
        this.caseNumber = caseNumber;
        this.amount = amount;
        this.partyType = partyType;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.contactEmailAddress = contactEmailAddress;
    }

    public List<String> toList() {
        return Arrays.asList(
            siteId,
            caseType,
            checkList,
            partyStatus,
            caseNumber,
            amount,
            partyType,
            contactName,
            contactNumber,
            contactEmailAddress
        );
    }

    @Override
    public Iterator<String> iterator() {
        return this.toList().iterator();
    }
}
