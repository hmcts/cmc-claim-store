package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;

@Value
@Builder
public class CCDCase {

    private Long id;
    private String referenceNumber;
    private String submitterId;
    private String submittedOn;
    private String externalId;
    private String issuedOn;
    private String submitterEmail;
    private CCDClaim claimData;
    private CCDCountyCourtJudgment countyCourtJudgment;
    private String countyCourtJudgmentRequestedAt;

}
