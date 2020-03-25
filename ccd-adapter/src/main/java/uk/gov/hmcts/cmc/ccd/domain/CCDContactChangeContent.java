package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CCDContactChangeContent {
    private String caseworkerName;
    private String telephone;
    private CCDYesNoOption isTelephoneModified;
    private String primaryEmail;
    private CCDYesNoOption isEmailModified;
    private CCDAddress primaryAddress;
    private CCDYesNoOption isPrimaryAddressModified;
    private CCDAddress correspondenceAddress;
    private CCDYesNoOption isCorrespondenceAddressModified;
    private CCDYesNoOption telephoneRemoved;
    private CCDYesNoOption primaryEmailRemoved;
    private CCDYesNoOption correspondenceAddressRemoved;
    private CCDYesNoOption letterNeededForDefendant;

    public boolean noContentChange() {
        return isTelephoneModified == NO
            && isEmailModified == NO
            && isPrimaryAddressModified == NO
            && isCorrespondenceAddressModified == NO
            && correspondenceAddressRemoved == NO
            && primaryEmailRemoved == NO
            && telephoneRemoved == NO;
    }
}
