package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@Component
public class DocAssemblyTemplateBodyMapper {

    private Clock clock;
    private final HearingCourtDetailsFinder hearingCourtDetailsFinder;

    @Autowired
    public DocAssemblyTemplateBodyMapper(Clock clock, HearingCourtDetailsFinder hearingCourtDetailsFinder) {
        this.clock = clock;
        this.hearingCourtDetailsFinder = hearingCourtDetailsFinder;
    }

    public DocAssemblyTemplateBody from(CCDCase ccdCase, UserDetails userDetails) {
        CCDOrderGenerationData ccdOrderGenerationData = ccdCase.getDirectionOrderData();

        HearingCourt hearingCourt = Optional.ofNullable(ccdOrderGenerationData.getHearingCourt())
            .map(hearingCourtDetailsFinder::findHearingCourtAddress)
            .orElseGet(() -> HearingCourt.builder().build());

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
            .currentDate(LocalDate.now(clock.withZone(UTC_ZONE)))
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
            .extraDocUploadList(
                ccdOrderGenerationData.getExtraDocUploadList())
            .eyewitnessUploadForParty(
                ccdOrderGenerationData.getEyewitnessUploadForParty())
            .paperDetermination(
                ccdOrderGenerationData.getPaperDetermination().toBoolean())
            .hearingCourtName(
                hearingCourt.getName())
            .hearingCourtAddress(
                hearingCourt.getAddress())
            .estimatedHearingDuration(
                ccdOrderGenerationData.getEstimatedHearingDuration())
            .otherDirections(
                ccdOrderGenerationData.getOtherDirections()
                    .stream()
                    .filter(direction -> direction != null && direction.getValue() != null)
                    .map(CCDCollectionElement::getValue)
                    .map(ccdOrderDirection -> OtherDirection.builder()
                        .directionComment(ccdOrderDirection.getDirectionComment())
                        .sendBy(ccdOrderDirection.getSendBy())
                        .expertReports(ccdOrderDirection.getExpertReports())
                        .extraDocUploadList(ccdOrderDirection.getExtraDocUploadList())
                        .extraOrderDirection(ccdOrderDirection.getExtraOrderDirection())
                        .forParty(ccdOrderDirection.getForParty())
                        .otherDirectionHeaders(ccdOrderDirection.getOtherDirectionHeaders())
                        .build())
                    .collect(Collectors.toList()))
            .build();
    }
}
