package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor //see https://github.com/rzwitserloot/lombok/issues/1347
@NoArgsConstructor
public class CCDOrderGenerationData {
    private LocalDate docUploadDeadline;

    private CCDDirectionPartyType docUploadForParty;

    private LocalDate eyewitnessUploadDeadline;

    private CCDDirectionPartyType eyewitnessUploadForParty;

    @Builder.Default
    private List<CCDOrderDirectionType> directionList = Collections.emptyList();

    @Builder.Default
    private List<CCDCollectionElement<CCDOrderDirection>> otherDirections = Collections.emptyList();

    @Builder.Default
    private List<CCDCollectionElement<String>> extraDocUploadList = Collections.emptyList();

    private CCDYesNoOption hearingRequired;

    private String newRequestedCourt;

    private String preferredCourtObjectingReason;

    private CCDHearingCourtType hearingCourt;

    private CCDHearingDurationType estimatedHearingDuration;

    private String hearingStatement;

    private CCDDocument draftOrderDoc;

}
