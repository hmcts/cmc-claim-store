package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public enum CCDHearingCourtType {
    EDMONTON("N18 2TN"),
    MANCHESTER("M60 9DJ"),
    BIRMINGHAM("B1 1AA"),
    CLERKENWELL("EC1V 3RE"),
    OTHER("");

    private String postcode;

    public String getPostcode() {
        return postcode;
    }
}
