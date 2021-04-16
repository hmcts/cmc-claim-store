package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.Objects.nonNull;

@Component
public class PaperDefenceLetterBodyMapper {

    public DocAssemblyTemplateBody coverLetterTemplateMapper(
        CCDCase ccdCase,
        String caseworkerName,
        LocalDate extendedResponseDeadline
    ) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        CCDParty givenRespondent = respondent.getClaimantProvidedDetail();
        CCDAddress defendantAddress = getDefendantAddress(respondent, givenRespondent);
        CCDApplicant applicant = ccdCase.getApplicants().get(0).getValue();

        LocalDate currentDate = LocalDate.now();

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
            .extendedResponseDeadline(extendedResponseDeadline)
            .caseworkerName(caseworkerName)
            .caseName(ccdCase.getCaseName())
            .soleTradingTraderName(ccdCase.getRespondents().get(0).getValue().getClaimantProvidedDetail()
                .getBusinessName())
            .moreTimeRequested(respondent.getResponseMoreTimeNeededOption().toBoolean())
            .build();
    }

    public DocAssemblyTemplateBody oconFormIndividualWithDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline,
                                                                   String courtName, boolean disableN9Form) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase, extendedDeadline, disableN9Form);
        return commonTemplate.toBuilder().preferredCourt(courtName).build();
    }

    public DocAssemblyTemplateBody oconFormIndividualWithoutDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline,
                                                                      boolean disableN9Form) {
        return oconFormCommonTemplateMapper(ccdCase, extendedDeadline, disableN9Form);
    }

    public DocAssemblyTemplateBody oconFormSoleTraderWithDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline,
                                                                   String courtName, boolean disableN9Form) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase, extendedDeadline, disableN9Form);
        return commonTemplate.toBuilder()
            .soleTradingTraderName(ccdCase.getRespondents().get(0).getValue().getClaimantProvidedDetail()
                .getBusinessName())
            .preferredCourt(courtName)
            .build();
    }

    public DocAssemblyTemplateBody oconFormSoleTraderWithoutDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline,
                                                                      boolean disableN9Form) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase, extendedDeadline, disableN9Form);
        return commonTemplate.toBuilder()
            .soleTradingTraderName(ccdCase.getRespondents().get(0).getValue().getClaimantProvidedDetail()
                .getBusinessName())
            .build();
    }

    public DocAssemblyTemplateBody oconFormOrganisationWithDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline,
                                                                     String courtName, boolean disableN9Form) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase, extendedDeadline, disableN9Form);
        return commonTemplate.toBuilder()
            .organisationName(ccdCase.getRespondents().get(0).getValue().getClaimantProvidedPartyName())
            .preferredCourt(courtName)
            .build();
    }

    public DocAssemblyTemplateBody oconFormOrganisationWithoutDQsMapper(CCDCase ccdCase, LocalDate extendedDeadline,
                                                                        boolean disableN9Form) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase, extendedDeadline, disableN9Form);
        return commonTemplate.toBuilder()
            .organisationName(ccdCase.getRespondents().get(0).getValue().getClaimantProvidedPartyName())
            .build();
    }

    public DocAssemblyTemplateBody oconFormCommonTemplateMapper(CCDCase ccdCase, LocalDate extendedResponseDeadline,
                                                                boolean disableN9Form) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        CCDApplicant applicant = ccdCase.getApplicants().get(0).getValue();
        CCDParty givenRespondent = respondent.getClaimantProvidedDetail();
        CCDAddress claimantAddress = applicant.getPartyDetail().getCorrespondenceAddress() == null
            ? applicant.getPartyDetail().getPrimaryAddress() : applicant.getPartyDetail().getCorrespondenceAddress();

        CCDAddress defendantAddress = getDefendantAddress(respondent, givenRespondent);

        String partyName = respondent.getPartyName() != null
            ? respondent.getPartyName() :
            respondent.getClaimantProvidedPartyName();

        return DocAssemblyTemplateBody.builder()
            .referenceNumber(ccdCase.getPreviousServiceCaseReference())
            .responseDeadline(respondent.getResponseDeadline())
            .extendedResponseDeadline(extendedResponseDeadline)
            .claimAmount(nonNull(ccdCase.getTotalAmount())
                ? String.valueOf(MonetaryConversions.penniesToPounds(new BigDecimal(ccdCase.getTotalAmount()))) : null)
            .partyName(partyName)
            .partyAddress(defendantAddress)
            .claimantName(applicant.getPartyName())
            .claimantPhone(applicant.getPartyDetail().getTelephoneNumber() != null
                ? applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber() : null)
            .claimantEmail(applicant.getPartyDetail().getEmailAddress())
            .claimantAddress(claimantAddress)
            .moreTimeRequested(disableN9Form)
            .build();
    }

    private CCDAddress getDefendantAddress(CCDRespondent respondent, CCDParty givenRespondent) {

        if (respondent.getPartyDetail() != null && respondent.getPartyDetail().getCorrespondenceAddress() != null) {
            return respondent.getPartyDetail().getCorrespondenceAddress();
        } else if (respondent.getPartyDetail() != null && respondent.getPartyDetail().getPrimaryAddress() != null) {
            return respondent.getPartyDetail().getPrimaryAddress();
        } else if (givenRespondent.getCorrespondenceAddress() != null) {
            return givenRespondent.getCorrespondenceAddress();
        } else {
            return givenRespondent.getPrimaryAddress();
        }
    }
}
