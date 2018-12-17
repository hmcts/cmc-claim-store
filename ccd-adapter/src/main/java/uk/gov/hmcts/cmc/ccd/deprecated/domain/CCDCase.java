package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.offers.CCDSettlement;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CCDCase {

    private Long id;
    private String referenceNumber;
    private String submitterId;
    private String letterHolderId;
    private String defendantId;
    private String submittedOn;
    private String externalId;
    private String issuedOn;
    private LocalDate responseDeadline;
    private CCDYesNoOption moreTimeRequested;
    private String submitterEmail;
    private CCDClaim claimData;
    private CCDCountyCourtJudgment countyCourtJudgment;
    private LocalDateTime countyCourtJudgmentRequestedAt;
    private String defendantEmail;
    private CCDResponse response;
    private LocalDateTime respondedAt;
    private CCDSettlement settlement;
    private LocalDateTime settlementReachedAt;
    private CCDDocument sealedClaimDocument;
    private String features;
    private LocalDate moneyReceivedOn;
    private CCDClaimantResponse claimantResponse;
    private LocalDateTime claimantRespondedAt;
    private LocalDate directionsQuestionnaireDeadline;
}
