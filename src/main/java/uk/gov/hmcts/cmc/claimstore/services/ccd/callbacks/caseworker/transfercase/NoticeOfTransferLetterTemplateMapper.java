package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferReason;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.ClaimFeatures;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@Component
public class NoticeOfTransferLetterTemplateMapper {

    private final Clock clock;

    public NoticeOfTransferLetterTemplateMapper(Clock clock, UserService userService) {
        this.clock = clock;
        this.userService = userService;
    }

    private final UserService userService;

    public DocAssemblyTemplateBody noticeOfTransferLetterBodyForCourt(CCDCase ccdCase, String authorisation) {

        LocalDate currentDate = now(clock.withZone(UTC_ZONE));
        CCDTransferContent transferContent = ccdCase.getTransferContent();

        return DocAssemblyTemplateBody.builder()
            .currentDate(currentDate)
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .hearingCourtName(transferContent.getTransferCourtName())
            .hearingCourtAddress(transferContent.getTransferCourtAddress())
            .caseworkerName(getCaseworkerName(authorisation))
            .caseName(ccdCase.getCaseName())
            .reasonForTransfer(getTransferReason(ccdCase))
            .orderDrawnByJudge(isOrderDrawnByJudge(ccdCase))
            .orderDrawnByLA(isOrderDrawnByLA(ccdCase))
            .build();
    }

    public DocAssemblyTemplateBody noticeOfTransferToCcbcLetterBodyForDefendant(CCDCase ccdCase, String authorisation) {
        return DocAssemblyTemplateBody.builder()
            .currentDate(now(clock.withZone(UTC_ZONE)))
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .caseworkerName(getCaseworkerName(authorisation))
            .caseName(ccdCase.getCaseName())
            .partyName(getDefendantName(ccdCase))
            .partyAddress(getDefendantAddress(ccdCase))
            .build();
    }

    public DocAssemblyTemplateBody noticeOfTransferLetterBodyForDefendant(CCDCase ccdCase, String authorisation) {

        String partyName = getDefendantName(ccdCase);
        CCDAddress partyAddress = getDefendantAddress(ccdCase);

        LocalDate currentDate = now(clock.withZone(UTC_ZONE));
        CCDTransferContent transferContent = ccdCase.getTransferContent();

        return DocAssemblyTemplateBody.builder()
            .currentDate(currentDate)
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .hearingCourtName(transferContent.getTransferCourtName())
            .hearingCourtAddress(transferContent.getTransferCourtAddress())
            .caseworkerName(getCaseworkerName(authorisation))
            .caseName(ccdCase.getCaseName())
            .partyName(partyName)
            .partyAddress(partyAddress)
            .reasonForTransfer(getTransferReason(ccdCase))
            .build();
    }

    private String getTransferReason(CCDCase ccdCase) {
        return ccdCase.getTransferContent().getTransferReason() == CCDTransferReason.OTHER
            ? ccdCase.getTransferContent().getTransferReasonOther()
            : ccdCase.getTransferContent().getTransferReason().getValue();
    }

    private String getDefendantName(CCDCase ccdCase) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0)
            .getValue();
        return respondent.getClaimantProvidedPartyName();
    }

    private CCDAddress getDefendantAddress(CCDCase ccdCase) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        return respondent.getClaimantProvidedDetail().getPrimaryAddress();
    }

    private String getCaseworkerName(String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        return userDetails.getFullName();
    }

    private boolean isOrderDrawnByJudge(CCDCase ccdCase) {
        return ccdCase.getDirectionOrder() != null && ccdCase.getFeatures()
            .contains(ClaimFeatures.JUDGE_PILOT_FLAG.getValue());
    }

    private boolean isOrderDrawnByLA(CCDCase ccdCase) {
        return ccdCase.getDirectionOrder() != null && ccdCase.getFeatures()
            .contains(ClaimFeatures.LA_PILOT_FLAG.getValue());
    }
}
