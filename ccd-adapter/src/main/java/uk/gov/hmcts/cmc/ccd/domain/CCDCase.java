package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;

import java.time.LocalDate;

@Data
@Builder
public class CCDCase {

    private Long id;
    private String referenceNumber;
    private String submitterId;
    private String submittedOn;
    private String externalId;
    private String issuedOn;
    private LocalDate responseDeadline;
    private boolean moreTimeRequested;
    private String submitterEmail;
    private CCDClaim claimData;
    private CCDCountyCourtJudgment countyCourtJudgment;
    private String countyCourtJudgmentRequestedAt;

}
