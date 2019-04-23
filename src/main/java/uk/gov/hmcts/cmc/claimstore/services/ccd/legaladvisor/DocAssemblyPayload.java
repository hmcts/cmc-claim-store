package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingDurationType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.reform.docassembly.domain.FormPayload;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@EqualsAndHashCode
public class DocAssemblyPayload implements FormPayload {

    private static final String HMCTS_URL = "https://www.gov.uk/make-money-claim";
    private static final String COURT_SEAL_IMG = "[userImage:courtseal.PNG]";

    private final int rowNum = 0;

    @JsonProperty("courtseal")
    private final String courtSeal = COURT_SEAL_IMG;

    private final boolean displayComments = false;

    private final String hmctsURL = HMCTS_URL;

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

    @JsonProperty("hearingisRequired")
    private boolean hearingIsRequired;

    private CCDDirectionPartyType docUploadForParty;

    private CCDDirectionPartyType eyewitnessUploadForParty;

    private String preferredCourtName;

    private String preferredCourtAddress;

    private CCDHearingDurationType estimatedHearingDuration;

    private String hearingStatement;

    private List<OrderDirection> otherDirectionList;

    @Builder
    @EqualsAndHashCode
    private static class Party {
        private String partyName;
    }

    @Builder
    @EqualsAndHashCode
    private static class Judicial {
        private String firstName;

        private String lastName;
    }

    @Builder
    @EqualsAndHashCode
    private static class OrderDirection {
        @JsonProperty("OtherDirection")
        private OtherDirection otherDirection;
    }

    @Builder
    @EqualsAndHashCode
    private static class OtherDirection {
        private CCDOrderDirectionType extraOrderDirection;
        private String directionComment;
        private LocalDate sendBy;
        private CCDDirectionPartyType forParty;
        private final String hmctsURL = HMCTS_URL;
    }

    public static DocAssemblyPayload from(CCDCase ccdCase, UserDetails userDetails) {
        return DocAssemblyPayload.builder()
            .claimant(Party.builder()
                .partyName(ccdCase.getApplicants()
                    .get(0)
                    .getValue()
                    .getPartyName())
                .build())
            .defendant(Party.builder()
                .partyName(ccdCase.getRespondents()
                    .get(0)
                    .getValue()
                    .getPartyName())
                .build())
            .judicial(Judicial.builder()
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname().orElse(""))
                .build())
            .currentDate(LocalDate.now())
            .referenceNumber(ccdCase.getReferenceNumber())
            .hasFirstOrderDirections(
                ccdCase.getDirectionList().contains(CCDOrderDirectionType.DOCUMENTS))
            .docUploadDeadline(
                ccdCase.getDocUploadDeadline())
            .hasSecondOrderDirections(
                ccdCase.getDirectionList().contains(CCDOrderDirectionType.EYEWITNESS))
            .eyewitnessUploadDeadline(
                ccdCase.getEyewitnessUploadDeadline())
            .docUploadForParty(
                ccdCase.getDocUploadForParty())
            .eyewitnessUploadForParty(
                ccdCase.getEyewitnessUploadForParty())
            .hearingIsRequired(
                ccdCase.getHearingIsRequired().toBoolean())
            .preferredCourtName(
                "Some court")    // will be populated when the acceptance criterias are refined
            .preferredCourtAddress(
                "this is an address EC2Y 3ND")
            .estimatedHearingDuration(
                ccdCase.getEstimatedHearingDuration())
            .hearingStatement(
                ccdCase.getHearingStatement())
            .otherDirectionList(
                ccdCase.getOtherDirectionList().stream().map(
                    direction -> OrderDirection.builder()
                        .otherDirection(OtherDirection.builder()
                            .extraOrderDirection(direction.getExtraOrderDirection())
                            .directionComment(direction.getOtherDirection())
                            .forParty(direction.getForParty())
                            .build())
                        .build()
                ).collect(Collectors.toList()))
            .build();
    }
}
