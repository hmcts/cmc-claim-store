package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferReason;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@Component
public class NoticeOfTransferLetterTemplateMapper {

    private final Clock clock;

    public NoticeOfTransferLetterTemplateMapper(Clock clock) {
        this.clock = clock;
    }

    public DocAssemblyTemplateBody noticeOfTransferLetterBodyForCourt(CCDCase ccdCase, String caseworkerName) {

        LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));
        CCDTransferContent transferContent = ccdCase.getTransferContent();

        return DocAssemblyTemplateBody.builder()
            .currentDate(currentDate)
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .hearingCourtName(transferContent.getTransferCourtName())
            .hearingCourtAddress(transferContent.getTransferCourtAddress())
            .caseworkerName(caseworkerName)
            .caseName(ccdCase.getCaseName())
            .reasonForTransfer(getTransferReason(ccdCase))
            .build();
    }

    public DocAssemblyTemplateBody noticeOfTransferLetterBodyForDefendant(CCDCase ccdCase, String caseworkerName) {

        String partyName = getDefendantName(ccdCase);
        CCDAddress partyAddress = getDefendantAddress(ccdCase);

        LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));
        CCDTransferContent transferContent = ccdCase.getTransferContent();

        return DocAssemblyTemplateBody.builder()
            .currentDate(currentDate)
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .hearingCourtName(transferContent.getTransferCourtName())
            .hearingCourtAddress(transferContent.getTransferCourtAddress())
            .caseworkerName(caseworkerName)
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
}
