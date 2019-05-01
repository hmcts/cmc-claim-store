package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CCDOrderGenerationData {

    private LocalDate docUploadDeadline;
    private CCDDirectionPartyType docUploadForParty;
    private LocalDate eyewitnessUploadDeadline;
    private CCDDirectionPartyType eyewitnessUploadForParty;
    private List<CCDOrderDirectionType> directionList;
    private List<CCDOrderDirection> otherDirectionList;
    private CCDYesNoOption hearingIsRequired;
    private String newRequestedCourt;
    private String preferredCourtObjectingReason;
    private CCDHearingCourtType hearingCourt;
    private CCDHearingDurationType estimatedHearingDuration;
    private String hearingStatement;
}
