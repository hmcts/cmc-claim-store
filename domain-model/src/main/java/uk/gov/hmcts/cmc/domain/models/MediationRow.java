package uk.gov.hmcts.cmc.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@EqualsAndHashCode
@Builder
@AllArgsConstructor
public class MediationRow implements Iterable<String> {
    private String siteId;
    private String caseNumber;
    private String caseType;
    private String amount;
    private String partyType;
    private String contactName;
    private String contactDetail;
    private String contactNumber;
    private String emailAddress;
    private String checkList;
    private String partyStatus;

    public List<String> toList() {
        return Arrays.asList(
            siteId,
            caseNumber,
            caseType,
            amount,
            partyType,
            contactName,
            contactDetail,
            contactNumber,
            checkList,
            partyStatus
        );
    }

    @Override
    public Iterator<String> iterator() {
        return toList().iterator();
    }
}
