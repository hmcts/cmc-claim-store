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
public class ContactChangeContent {
    private String caseworkerName;
    private String claimantName;
    private String claimantPhone;
    private String claimantEmail;
    private boolean hasPhoneChanged;
    private boolean hasEmailChanged;
    private boolean hasMainAddressChanged;
    private CCDAddress claimantAddress;
    private boolean hasContactAddressChanged;
    private CCDAddress claimantContactAddress;
    private boolean claimantPhoneRemoved;
    private boolean claimantEmailRemoved;
    private boolean claimantContactAddressRemoved;
}
