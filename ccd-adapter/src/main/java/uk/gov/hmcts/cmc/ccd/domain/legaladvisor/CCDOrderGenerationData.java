package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@AllArgsConstructor //see https://github.com/rzwitserloot/lombok/issues/1347
@NoArgsConstructor
@EqualsAndHashCode
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

    private CCDYesNoOption paperDetermination;

    private String newRequestedCourt;

    private String preferredDQCourt;

    private String preferredCourtObjectingReason;

    private String hearingCourt;

    private CCDHearingDurationType estimatedHearingDuration;

    private CCDDocument draftOrderDoc;

    private CCDYesNoOption expertReportPermissionPartyAskedByClaimant;
    private CCDYesNoOption expertReportPermissionPartyAskedByDefendant;
    private CCDYesNoOption grantExpertReportPermission;

    //TODO - Remove once CCD 1.5.9 released
    private CCDYesNoOption expertReportPermissionPartyGivenToClaimant;
    private CCDYesNoOption expertReportPermissionPartyGivenToDefendant;
    @Builder.Default
    private List<CCDCollectionElement<String>> expertReportInstructionClaimant = Collections.emptyList();
    @Builder.Default
    private List<CCDCollectionElement<String>> expertReportInstructionDefendant = Collections.emptyList();

    private String expertReportInstruction;
}
