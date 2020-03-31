    package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Component;
    import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
    import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
    import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
    import uk.gov.hmcts.cmc.ccd.domain.CCDContactChangeContent;
    import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
    import uk.gov.hmcts.cmc.ccd.domain.GeneralLetterContent;
    import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
    import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
    import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
    import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
    import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;

    import java.time.Clock;
    import java.time.LocalDate;
    import java.util.stream.Collectors;

    import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
    import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

    @Component
    public class DocAssemblyTemplateBodyMapper {

        public static final long DIRECTION_DEADLINE_NO_OF_DAYS = 19L;
        public static final long CHANGE_ORDER_DEADLINE_NO_OF_DAYS = 12L;
        private final Clock clock;
        private final DirectionOrderService directionOrderService;
        private final WorkingDayIndicator workingDayIndicator;

        @Autowired
        public DocAssemblyTemplateBodyMapper(
            Clock clock,
            DirectionOrderService directionOrderService,
            WorkingDayIndicator workingDayIndicator
        ) {
            this.clock = clock;
            this.directionOrderService = directionOrderService;
            this.workingDayIndicator = workingDayIndicator;
        }

        public DocAssemblyTemplateBody from(CCDCase ccdCase, UserDetails userDetails) {

            HearingCourt hearingCourt = directionOrderService.getHearingCourt(ccdCase);

            LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));
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
                .currentDate(currentDate)
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .hasFirstOrderDirections(ccdCase.getDirectionList().contains(CCDOrderDirectionType.DOCUMENTS))
                .docUploadDeadline(ccdCase.getDocUploadDeadline())
                .hasSecondOrderDirections(ccdCase.getDirectionList().contains(CCDOrderDirectionType.EYEWITNESS))
                .eyewitnessUploadDeadline(ccdCase.getEyewitnessUploadDeadline())
                .docUploadForParty(ccdCase.getDocUploadForParty())
                .extraDocUploadList(ccdCase.getExtraDocUploadList())
                .eyewitnessUploadForParty(ccdCase.getEyewitnessUploadForParty())
                .paperDetermination(ccdCase.getPaperDetermination() == YES)
                .hearingCourtName(hearingCourt.getName())
                .hearingCourtAddress(hearingCourt.getAddress())
                .estimatedHearingDuration(ccdCase.getEstimatedHearingDuration())
                .otherDirections(ccdCase.getOtherDirections()
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
                .directionDeadline(workingDayIndicator.getNextWorkingDay(
                    currentDate.plusDays(DIRECTION_DEADLINE_NO_OF_DAYS)))
                .changeOrderDeadline(workingDayIndicator.getNextWorkingDay(
                    currentDate.plusDays(CHANGE_ORDER_DEADLINE_NO_OF_DAYS)))
                .expertReportInstruction(ccdCase.getExpertReportInstruction())
                .expertReportPermissionPartyAskedByClaimant(ccdCase.getExpertReportPermissionPartyAskedByClaimant() == YES)
                .expertReportPermissionPartyAskedByDefendant(ccdCase
                    .getExpertReportPermissionPartyAskedByDefendant() == YES)
                .grantExpertReportPermission(ccdCase.getGrantExpertReportPermission() == YES)
                .expertReportInstructionClaimant(ccdCase.getExpertReportInstructionClaimant())
                .expertReportInstructionDefendant(ccdCase.getExpertReportInstructionDefendant())
                .expertReportPermissionPartyGivenToClaimant(ccdCase.getExpertReportPermissionPartyGivenToClaimant() == YES)
                .expertReportPermissionPartyGivenToDefendant(
                    ccdCase.getExpertReportPermissionPartyGivenToDefendant() == YES)
                .build();
        }

        public DocAssemblyTemplateBody generalLetterBody(CCDCase ccdCase) {
            LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));
            GeneralLetterContent generalLetterContent = ccdCase.getGeneralLetterContent();
            String partyName;
            CCDAddress partyAddress;
            if (ccdCase.getGeneralLetterContent().getIssueLetterContact()
                .equals(CCDContactPartyType.CLAIMANT)) {
                partyName = ccdCase.getApplicants().get(0).getValue().getPartyName();
                partyAddress = ccdCase.getApplicants().get(0)
                    .getValue().getPartyDetail().getPrimaryAddress();
            } else {
                partyName = ccdCase.getRespondents().get(0)
                    .getValue().getPartyName() != null
                    ? ccdCase.getRespondents().get(0).getValue().getPartyName() :
                    ccdCase.getRespondents().get(0).getValue().getClaimantProvidedPartyName();
                partyAddress = getDefendantAddress(ccdCase.getRespondents().get(0).getValue());
            }
            return DocAssemblyTemplateBody.builder()
                .currentDate(currentDate)
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .caseworkerName(generalLetterContent.getCaseworkerName())
                .caseName(ccdCase.getCaseName())
                .partyName(partyName)
                .partyAddress(partyAddress)
                .body(generalLetterContent.getLetterContent())
                .build();
        }

        public DocAssemblyTemplateBody changeContactBody(CCDCase ccdCase) {
            LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));
            CCDContactChangeContent contactChangeContent = ccdCase.getContactChangeContent();
            String defendantName = ccdCase.getRespondents().get(0).getValue().getClaimantProvidedPartyName();
            String claimantName = ccdCase.getApplicants().get(0).getValue().getPartyName();
            CCDAddress defendantAddress = getDefendantAddress(ccdCase.getRespondents().get(0).getValue());

            return DocAssemblyTemplateBody.builder()
                .currentDate(currentDate)
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .caseworkerName(contactChangeContent.getCaseworkerName())
                .caseName(ccdCase.getCaseName())
                .partyName(defendantName)
                .partyAddress(defendantAddress)
                .claimantName(claimantName)
                .claimantAddress(contactChangeContent.getPrimaryAddress())
                .hasMainAddressChanged(contactChangeContent.getIsPrimaryAddressModified().toBoolean())
                .hasEmailChanged(contactChangeContent.getIsEmailModified().toBoolean())
                .hasPhoneChanged(contactChangeContent.getIsTelephoneModified().toBoolean())
                .claimantEmail(contactChangeContent.getPrimaryEmail())
                .claimantPhone(contactChangeContent.getTelephone())
                .claimantContactAddress(contactChangeContent.getCorrespondenceAddress())
                .hasContactAddressChanged(contactChangeContent.getIsCorrespondenceAddressModified().toBoolean())
                .claimantEmailRemoved(contactChangeContent.getPrimaryEmailRemoved().toBoolean())
                .claimantPhoneRemoved(contactChangeContent.getTelephoneRemoved().toBoolean())
                .claimantContactAddressRemoved(contactChangeContent.getCorrespondenceAddressRemoved().toBoolean())
                .build();
        }

        private CCDAddress getDefendantAddress(CCDRespondent respondent) {
            return respondent.getPartyDetail() != null
                ? respondent.getPartyDetail().getPrimaryAddress()
                : respondent.getClaimantProvidedDetail().getPrimaryAddress();
        }
    }
