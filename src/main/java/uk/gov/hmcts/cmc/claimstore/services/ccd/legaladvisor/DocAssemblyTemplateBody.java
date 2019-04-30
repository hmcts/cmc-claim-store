package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingDurationType;
import uk.gov.hmcts.reform.docassembly.domain.FormPayload;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.TemplateConstants.HMCTS_URL;

@Builder
@Value
public class DocAssemblyTemplateBody implements FormPayload {
    private final int rowNum = 0;

    @JsonProperty("courtseal")
    private final String courtSeal = "[userImage:courtseal.PNG]";

    private final boolean displayComments = false;

    private final String hmctsURL = HMCTS_URL;

    @JsonProperty("displaycode")
    private final int displayCode = 0;

    private String referenceNumber;

    private Party claimant;

    private Party defendant;

    private Judicial judicial;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate currentDate;

    @JsonProperty("orderDirections1isIncluded")
    private boolean hasFirstOrderDirections;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate docUploadDeadline;

    @JsonProperty("orderDirections2isIncluded")
    private boolean hasSecondOrderDirections;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate eyewitnessUploadDeadline;

    @JsonProperty("hearingisRequired")
    private boolean hearingRequired;

    private CCDDirectionPartyType docUploadForParty;

    private CCDDirectionPartyType eyewitnessUploadForParty;

    private String preferredCourtName;

    private String preferredCourtAddress;

    private CCDHearingDurationType estimatedHearingDuration;

    private String hearingStatement;

    @JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
    private List<OtherDirection> otherDirectionList;
}
