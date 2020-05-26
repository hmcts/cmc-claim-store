package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

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

    public DocAssemblyTemplateBody noticeOfTransferLetterBodyForCourt(CCDCase ccdCase, String caseworkerName) {

        String partyName = ccdCase.getTransferContent().getNameOfTransferCourt();
        CCDAddress partyAddress = ccdCase.getTransferContent().getAddressOfTransferCourt();

        return noticeOfTransferLetterBody(ccdCase, partyName, partyAddress, caseworkerName);
    }

    public DocAssemblyTemplateBody noticeOfTransferLetterBodyForDefendant(CCDCase ccdCase, String caseworkerName) {

        String partyName = getDefendantName(ccdCase);
        CCDAddress partyAddress = getDefendantAddress(ccdCase);

        return noticeOfTransferLetterBody(ccdCase, partyName, partyAddress, caseworkerName);
    }

    private DocAssemblyTemplateBody noticeOfTransferLetterBody(CCDCase ccdCase,
                                                               String partyName,
                                                               CCDAddress partyAddress,
                                                               String caseworkerName) {

        LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));

        return DocAssemblyTemplateBody.builder()
            .currentDate(currentDate)
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .transferredCourtName(ccdCase.getTransferContent().getNameOfTransferCourt())
            .transferredCourtAddress(ccdCase.getTransferContent().getAddressOfTransferCourt())
            .caseworkerName(caseworkerName)
            .caseName(ccdCase.getCaseName())
            .partyName(partyName)
            .partyAddress(partyAddress)
            .reasonForTransfer(ccdCase.getTransferContent().getReasonForTransfer())
            .build();
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
