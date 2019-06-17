package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public enum CCDHearingCourtType {
    EDMONTON("N182TN"),
    MANCHESTER("M609DJ"),
    BIRMINGHAM("B11AA"),
    CLERKENWELL("EC1V3RE"),
    DEFENDANT_COURT,
    CLAIMANT_COURT;

    private String postcode;

    public String getPostcode() {
        return postcode;
    }
}
