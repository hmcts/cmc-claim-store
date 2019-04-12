package uk.gov.hmcts.cmc.claimstore.services.legaladvisor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingTimeType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDPartyForDirectionType;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
public class DocAssemblyRequest {

    private FormPayload formPayload;

    private final String outputType = "DOC";

    private String templateId;

    @Builder
    @EqualsAndHashCode
    public static class FormPayload {
        private final int rowNum = 0;

        @JsonProperty("courtseal")
        private final String courtSeal = "[userImage:courtseal.PNG]";

        private final boolean displayComments = false;

        private final String hmctsURL = "https://www.gov.uk/make-money-claim";

        @JsonProperty("displaycode")
        private final int displayCode = 0;

        private String referenceNumber;

        private Party claimant;

        private Party defendant;

        private Judicial judicial;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        private LocalDate currentDate;

        @JsonProperty("orderDirections1isIncluded")
        private boolean hasFirstOrderDirections;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        private LocalDate docUploadDeadline;

        @JsonProperty("orderDirections2isIncluded")
        private boolean hasSecondOrderDirections;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        private LocalDate eyewitnessUploadDeadline;

        @JsonProperty("orderDirections3isIncluded")
        private boolean hasThirdOrderDirections;

        @JsonProperty("hearingisRequired")
        private boolean hearingIsRequired;

        private CCDPartyForDirectionType docUploadForParty;

        private CCDPartyForDirectionType eyewitnessUploadForParty;

        private CCDPartyForDirectionType mediationForParty;

        private String preferredCourtName;

        private String preferredCourtAddress;

        private CCDHearingTimeType estimatedHearingDuration;

        private String hearingStatement;

        private List<CCDOrderDirection> otherDirectionList;
    }

    @Builder
    @EqualsAndHashCode
    public static class Judicial {
        private String firstName;

        private String lastName;
    }

    @Builder
    @EqualsAndHashCode
    public static class Party {
        private String partyName;
    }
}
