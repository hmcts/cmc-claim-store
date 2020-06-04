package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
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
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        CCDParty givenRespondent = respondent.getClaimantProvidedDetail();
        CCDAddress defendantAddress = givenRespondent.getCorrespondenceAddress() == null
                ? givenRespondent.getPrimaryAddress() : givenRespondent.getCorrespondenceAddress();
        CCDParty applicant = ccdCase.getApplicants().get(0).getValue().getPartyDetail();

        LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));

        return DocAssemblyTemplateBody.builder()
                .partyName(String.join(" ", givenRespondent.getTitle(),
                        givenRespondent.getFirstName(), givenRespondent.getLastName()))
                .partyAddress(defendantAddress)
                .claimantName(String.join(" ", applicant.getTitle(), applicant.getFirstName(), applicant.getLastName()))
                .currentDate(currentDate)
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .hearingCourtName(ccdCase.getHearingCourtName())
                .hearingCourtAddress(ccdCase.getHearingCourtAddress())
                .caseworkerName(caseworkerName)
                .caseName(ccdCase.getCaseName())
                .build();
    }

    private DocAssemblyTemplateBody oconFormCommonTemplateMapper(CCDCase ccdCase) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        CCDParty applicant = ccdCase.getApplicants().get(0).getValue().getPartyDetail();
        CCDParty givenRespondent = respondent.getClaimantProvidedDetail();
        CCDAddress claimantAddress = applicant.getCorrespondenceAddress() == null
                ? applicant.getPrimaryAddress() : applicant.getCorrespondenceAddress();
        //does this work if the defendant is a company
        CCDAddress defendantAddress = givenRespondent.getCorrespondenceAddress() == null
                ? givenRespondent.getPrimaryAddress() : givenRespondent.getCorrespondenceAddress();

        return DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(respondent.getExtendedResponseDeadline())
                .claimAmount(ccdCase.getTotalAmount())
                //do I need specific defendant (address) attribute?
                .partyAddress(defendantAddress)
                .claimantName(String.join(" ", applicant.getTitle(), applicant.getFirstName(), applicant.getLastName()))
                .claimantPhone(applicant.getTelephoneNumber().toString())
                .claimantEmail(applicant.getEmailAddress())
                .claimantAddress(claimantAddress)
                .build();
    }

    public DocAssemblyTemplateBody oconFormIndividualWithDQsTemplateMapper(CCDCase ccdCase) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase);
        return  commonTemplate.toBuilder().preferredCourt(ccdCase.getPreferredDQCourt()).build();
    }

    public DocAssemblyTemplateBody oconFormIndividualWithoutDQsTemplateMapper(CCDCase ccdCase) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase);
        return  commonTemplate;
    }

    public DocAssemblyTemplateBody oconFormSoleTraderWithDQsTemplateMapper(CCDCase ccdCase) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase);
        return  commonTemplate.toBuilder()
                .soleTradingTraderName(ccdCase.getRespondents().get(0).getValue().getPartyDetail().getBusinessName())
                .preferredCourt(ccdCase.getPreferredDQCourt())
                .build();
    }

    public DocAssemblyTemplateBody oconFormSoleTraderWithoutDQsTemplateMapper(CCDCase ccdCase) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase);
        return commonTemplate.toBuilder()
                .soleTradingTraderName(ccdCase.getRespondents().get(0).getValue().getPartyDetail().getBusinessName())
                .build();
    }

    public DocAssemblyTemplateBody oconFormOrganisationWithDQsTemplateMapper(CCDCase ccdCase) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase);
        return  commonTemplate.toBuilder()
                .organisationName(ccdCase.getRespondents().get(0).getValue().getClaimantProvidedPartyName())
                .preferredCourt(ccdCase.getPreferredDQCourt())
                .build();
    }

    public DocAssemblyTemplateBody oconFormOrganisationWithoutDQsTemplateMapper(CCDCase ccdCase) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase);
        return commonTemplate.toBuilder()
                .organisationName(ccdCase.getRespondents().get(0).getValue().getClaimantProvidedPartyName())
                .build();
    }
}
