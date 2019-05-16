package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingCourtType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Address;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DocAssemblyTemplateBodyMapper {

    private Clock clock;
    private final CourtFinderApi courtFinderApi;

    @Autowired
    public DocAssemblyTemplateBodyMapper(Clock clock, CourtFinderApi courtFinderApi) {
        this.clock = clock;
        this.courtFinderApi = courtFinderApi;
    }

    public DocAssemblyTemplateBody from(CCDCase ccdCase,
                                        UserDetails userDetails) {
        CCDOrderGenerationData ccdOrderGenerationData = ccdCase.getOrderGenerationData();
        HearingCourt hearingCourt = mapHearingCourt(ccdCase, ccdOrderGenerationData.getHearingCourt());
        return DocAssemblyTemplateBody.builder()
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
            .currentDate(LocalDate.now(clock))
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .hasFirstOrderDirections(
                ccdOrderGenerationData.getDirectionList().contains(CCDOrderDirectionType.DOCUMENTS))
            .docUploadDeadline(
                ccdOrderGenerationData.getDocUploadDeadline())
            .hasSecondOrderDirections(
                ccdOrderGenerationData.getDirectionList().contains(CCDOrderDirectionType.EYEWITNESS))
            .eyewitnessUploadDeadline(
                ccdOrderGenerationData.getEyewitnessUploadDeadline())
            .docUploadForParty(
                ccdOrderGenerationData.getDocUploadForParty())
            .eyewitnessUploadForParty(
                ccdOrderGenerationData.getEyewitnessUploadForParty())
            .hearingRequired(
                ccdOrderGenerationData.getHearingIsRequired().toBoolean())
            .hearingCourtName(
                hearingCourt.getName())
            .hearingCourtAddress(
                hearingCourt.getAddress())
            .estimatedHearingDuration(
                ccdOrderGenerationData.getEstimatedHearingDuration())
            .hearingStatement(
                ccdOrderGenerationData.getHearingStatement())
            .otherDirectionList(
                ccdOrderGenerationData.getOtherDirectionList()
                    .stream()
                    .filter(direction -> direction != null && direction.getValue() != null)
                    .map(direction -> OtherDirection.builder()
                            .extraOrderDirection(direction.getValue().getExtraOrderDirection())
                            .directionComment(direction.getValue().getOtherDirection())
                            .forParty(direction.getValue().getForParty())
                            .sendBy(direction.getValue().getSendBy())
                            .build()
                ).collect(Collectors.toList()))
            .build();
    }

    private CCDAddress mapHearingAddress(Address address) {
        CCDAddress.CCDAddressBuilder ccdAddressBuilder = CCDAddress.builder()
            .postTown(address.getTown())
            .postCode(address.getPostcode());

        try {
            ccdAddressBuilder.addressLine1(address.getAddressLines().get(0));
            ccdAddressBuilder.addressLine2(address.getAddressLines().get(1));
            ccdAddressBuilder.addressLine3(address.getAddressLines().get(2));
        } catch (ArrayIndexOutOfBoundsException exc) {
            //the address line out of bounds is going to be set as null, which is ok
        }
        return ccdAddressBuilder.build();
    }

    private HearingCourt mapHearingCourt(CCDCase ccdCase, CCDHearingCourtType courtType) {
        HearingCourt.HearingCourtBuilder courtBuilder = HearingCourt.builder();
        switch (courtType) {
            case CLAIMANT_COURT:
                CCDApplicant applicant = ccdCase.getApplicants().get(0).getValue();
                return courtBuilder.name(applicant.getPreferredCourtName())
                    .address(applicant.getPreferredCourtAddress())
                    .build();
            case DEFENDANT_COURT:
                CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
                return courtBuilder.name(respondent.getPreferredCourtName())
                    .address(respondent.getPreferredCourtAddress())
                    .build();
            default:
                return courtFinderApi.findMoneyClaimCourtByPostcode(courtType.getPostcode())
                    .stream()
                    .findFirst()
                    .map(court -> HearingCourt.builder()
                        .name(court.getName())
                        .address(mapHearingAddress(court.getAddress()))
                        .build())
                    .orElseThrow(IllegalArgumentException::new);
        }
    }
}
