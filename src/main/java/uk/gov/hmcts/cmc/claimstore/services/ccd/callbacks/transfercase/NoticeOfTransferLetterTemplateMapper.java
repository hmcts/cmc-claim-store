package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.transfercase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
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

    public DocAssemblyTemplateBody noticeOfTransferLetterBody(CCDCase ccdCase,
                                                              NoticeOfTransferLetterType noticeOfTransferLetterType) {

        LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));

        String partyName;
        CCDAddress partyAddress;

        switch (noticeOfTransferLetterType) {
            case FOR_COURT:
                partyName = ccdCase.getTransferContent().getNameOfTransferCourt();
                partyAddress = ccdCase.getTransferContent().getAddressOfTransferCourt();
                break;
            case FOR_DEFENDANT:
                partyName = getDefendantName(ccdCase);
                partyAddress = getDefendantAddress(ccdCase);
                break;
            default:
                throw new IllegalArgumentException();
        }

        return DocAssemblyTemplateBody.builder()
            .currentDate(currentDate)
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .caseworkerName(ccdCase.getTransferContent().getCaseworkerName())
            .caseName(ccdCase.getCaseName())
            .partyName(partyName)
            .partyAddress(partyAddress)
            .reasonForTransfer(ccdCase.getTransferContent().getReasonForTransfer())
            .build();
    }

    private String getDefendantName(CCDCase ccdCase) {
        return ccdCase.getRespondents().get(0)
            .getValue().getPartyName() != null
            ? ccdCase.getRespondents().get(0).getValue().getPartyName() :
            ccdCase.getRespondents().get(0).getValue().getClaimantProvidedPartyName();
    }

    private CCDAddress getDefendantAddress(CCDCase ccdCase) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        return respondent.getPartyDetail() != null
            ? respondent.getPartyDetail().getPrimaryAddress()
            : respondent.getClaimantProvidedDetail().getPrimaryAddress();
    }
}
