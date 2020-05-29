package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.IssuePaperDefenceForms;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@Component
public class PaperDefenceLetterBodyMapper {
    private final Clock clock;

    public PaperDefenceLetterBodyMapper(Clock clock) {
        this.clock = clock;
    }

    public DocAssemblyTemplateBody coverLetterTemplateMapper(CCDCase ccdCase, String caseworkerName) {

        LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));

        return DocAssemblyTemplateBody.builder()
                //change variables
                .currentDate(currentDate)
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .hearingCourtName(ccdCase.getHearingCourtName())
                .hearingCourtAddress(ccdCase.getHearingCourtAddress())
                .caseworkerName(caseworkerName)
                .caseName(ccdCase.getCaseName())
                .build();
    }

    //different versions?
    public DocAssemblyTemplateBody oconFormTemplateMapper(CCDCase ccdCase, String caseworkerName) {

        LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));

        return DocAssemblyTemplateBody.builder()
                //change variables
                .currentDate(currentDate)
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .hearingCourtName(ccdCase.getHearingCourtName())
                .hearingCourtAddress(ccdCase.getHearingCourtAddress())
                .caseworkerName(caseworkerName)
                .caseName(ccdCase.getCaseName())
                .build();
    }
}
