package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CCDContactChangeContent {
    private String caseworkerName;
    private String claimantName;
    private String claimantPhone;
    private String claimantEmail;
    private CCDYesNoOption hasPhoneChanged;
    private CCDYesNoOption hasEmailChanged;
    private CCDYesNoOption hasMainAddressChanged;
    private CCDAddress claimantAddress;
    private CCDYesNoOption hasContactAddressChanged;
    private CCDAddress claimantContactAddress;
    private CCDYesNoOption claimantPhoneRemoved;
    private CCDYesNoOption claimantEmailRemoved;
    private CCDYesNoOption claimantContactAddressRemoved;
}
