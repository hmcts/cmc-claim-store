package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class PaperDefenceLetterBodyMapper {
    private final Clock clock;

    public PaperDefenceLetterBodyMapper(Clock clock) {
        this.clock = clock;
    }

    public DocAssemblyTemplateBody coverLetterTemplateMapper(
        CCDCase ccdCase,
        String caseworkerName,
        LocalDate extendedResponseDeadline
    ) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        CCDParty givenRespondent = respondent.getClaimantProvidedDetail();
        CCDAddress defendantAddress = givenRespondent.getCorrespondenceAddress() == null
            ? givenRespondent.getPrimaryAddress() : givenRespondent.getCorrespondenceAddress();
        CCDApplicant applicant = ccdCase.getApplicants().get(0).getValue();

        LocalDate currentDate = LocalDate.now();

        // TODO how to create party name either based on title + first name + last name or just party name
        String partyName = respondent.getPartyName() != null
            ? respondent.getPartyName() :
            respondent.getClaimantProvidedPartyName();

        return DocAssemblyTemplateBody.builder()
            .partyName(partyName)
            .partyAddress(defendantAddress)
            .claimantName(applicant.getPartyName())
            .currentDate(currentDate)
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .responseDeadline(respondent.getResponseDeadline())
            .updatedResponseDeadline(extendedResponseDeadline)
            .caseworkerName(caseworkerName)
            .caseName(ccdCase.getCaseName())
            .build();
    }

    public DocAssemblyTemplateBody oconFormIndividualWithDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase, extendedDeadline);
        return commonTemplate.toBuilder().preferredCourt(ccdCase.getPreferredDQCourt()).build();
    }

    public DocAssemblyTemplateBody oconFormIndividualWithoutDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline) {
        return oconFormCommonTemplateMapper(ccdCase, extendedDeadline);
    }

    public DocAssemblyTemplateBody oconFormSoleTraderWithDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase, extendedDeadline);
        return commonTemplate.toBuilder()
            .soleTradingTraderName(ccdCase.getRespondents().get(0).getValue().getPartyDetail().getBusinessName())
            .preferredCourt(ccdCase.getPreferredDQCourt())
            .build();
    }

    public DocAssemblyTemplateBody oconFormSoleTraderWithoutDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase, extendedDeadline);
        return commonTemplate.toBuilder()
            .soleTradingTraderName(ccdCase.getRespondents().get(0).getValue().getPartyDetail().getBusinessName())
            .build();
    }

    public DocAssemblyTemplateBody oconFormOrganisationWithDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase, extendedDeadline);
        return commonTemplate.toBuilder()
            .organisationName(ccdCase.getRespondents().get(0).getValue().getClaimantProvidedPartyName())
            .preferredCourt(ccdCase.getPreferredDQCourt())
            .build();
    }

    public DocAssemblyTemplateBody oconFormOrganisationWithoutDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase, extendedDeadline);
        return commonTemplate.toBuilder()
            .organisationName(ccdCase.getRespondents().get(0).getValue().getClaimantProvidedPartyName())
            .build();
    }

    public DocAssemblyTemplateBody oconFormCommonTemplateMapper(CCDCase ccdCase, LocalDate extendedResponseDeadline) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        CCDApplicant applicant = ccdCase.getApplicants().get(0).getValue();
        CCDParty givenRespondent = respondent.getClaimantProvidedDetail();
        CCDAddress claimantAddress = applicant.getPartyDetail().getCorrespondenceAddress() == null
            ? applicant.getPartyDetail().getPrimaryAddress() : applicant.getPartyDetail().getCorrespondenceAddress();
        CCDAddress defendantAddress = givenRespondent.getCorrespondenceAddress() == null
            ? givenRespondent.getPrimaryAddress() : givenRespondent.getCorrespondenceAddress();

        String partyName = respondent.getPartyName() != null
            ? respondent.getPartyName() :
            respondent.getClaimantProvidedPartyName();

        return DocAssemblyTemplateBody.builder()
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .responseDeadline(respondent.getResponseDeadline())
            .updatedResponseDeadline(extendedResponseDeadline)
            .claimAmount(ccdCase.getTotalAmount())
            .partyName(partyName)
            .partyAddress(defendantAddress)
            .claimantName(applicant.getPartyName())
            .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
            .claimantEmail(applicant.getPartyDetail().getEmailAddress())
            .claimantAddress(claimantAddress)
            .build();
    }
}
