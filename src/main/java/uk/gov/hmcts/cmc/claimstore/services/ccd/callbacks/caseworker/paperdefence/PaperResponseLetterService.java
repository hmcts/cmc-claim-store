package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;

import java.time.LocalDate;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class PaperResponseLetterService {

    public static final String LETTER_NAME = "%s-issue-paper-form.pdf";

    private final String oconFormIndividualWithDQs;
    private final String oconFormSoleTraderWithDQs;
    private final String oconFormOrganisationWithDQs;
    private final String oconFormIndividualWithoutDQs;
    private final String oconFormSoleTraderWithoutDQs;
    private final String oconFormOrganisationWithoutDQs;
    private final String paperDefenceCoverLetterTemplateID;
    private final String oconN9FormTemplateID;
    private final PaperDefenceLetterBodyMapper paperDefenceLetterBodyMapper;
    private final DocAssemblyService docAssemblyService;
    private final UserService userService;
    private final GeneralLetterService generalLetterService;
    private final CourtFinderApi courtFinderApi;

    @Autowired
    public PaperResponseLetterService(
        @Value("${doc_assembly.oconFormIndividualWithDQs}") String oconFormIndividualWithDQs,
        @Value("${doc_assembly.oconFormSoleTraderWithDQs}") String oconFormSoleTraderWithDQs,
        @Value("${doc_assembly.oconFormOrganisationWithDQs}") String oconFormOrganisationWithDQs,
        @Value("${doc_assembly.oconFormIndividualWithoutDQs}") String oconFormIndividualWithoutDQs,
        @Value("${doc_assembly.oconFormSoleTraderWithoutDQs}") String oconFormSoleTraderWithoutDQs,
        @Value("${doc_assembly.oconFormOrganisationWithoutDQs}") String oconFormOrganisationWithoutDQs,
        @Value("${doc_assembly.paperDefenceCoverLetterTemplateID}") String paperDefenceCoverLetterTemplateID,
        @Value("${doc_assembly.oconN9FormTemplateID}") String oconN9FormTemplateID,
        PaperDefenceLetterBodyMapper paperDefenceLetterBodyMapper,
        DocAssemblyService docAssemblyService,
        UserService userService,
        GeneralLetterService generalLetterService,
        CourtFinderApi courtFinderApi
    ) {
        this.oconFormIndividualWithDQs = oconFormIndividualWithDQs;
        this.oconFormSoleTraderWithDQs = oconFormSoleTraderWithDQs;
        this.oconFormOrganisationWithDQs = oconFormOrganisationWithDQs;
        this.oconFormIndividualWithoutDQs = oconFormIndividualWithoutDQs;
        this.oconFormSoleTraderWithoutDQs = oconFormSoleTraderWithoutDQs;
        this.oconFormOrganisationWithoutDQs = oconFormOrganisationWithoutDQs;
        this.paperDefenceLetterBodyMapper = paperDefenceLetterBodyMapper;
        this.paperDefenceCoverLetterTemplateID = paperDefenceCoverLetterTemplateID;
        this.docAssemblyService = docAssemblyService;
        this.userService = userService;
        this.generalLetterService = generalLetterService;
        this.courtFinderApi = courtFinderApi;
        this.oconN9FormTemplateID = oconN9FormTemplateID;
    }

    public CCDDocument createCoverLetter(CCDCase ccdCase, String authorisation, LocalDate extendedResponseDeadline) {
        var formPayloadForCoverLetter = getDocAssemblyTemplateBody(ccdCase, authorisation,
            extendedResponseDeadline);

        return docAssemblyService.generateDocument(ccdCase,
            authorisation,
            formPayloadForCoverLetter,
            paperDefenceCoverLetterTemplateID);
    }

    private DocAssemblyTemplateBody getDocAssemblyTemplateBody(CCDCase ccdCase, String authorisation,
                                                               LocalDate extendedResponseDeadline) {
        return paperDefenceLetterBodyMapper.coverLetterTemplateMapper(
            ccdCase, getCaseWorkerName(authorisation), extendedResponseDeadline);
    }

    public CCDDocument createOCON9From(CCDCase ccdCase, String authorisation, LocalDate extendedResponseDeadline) {
        var formPayloadForCoverLetter = getDocAssemblyTemplateBody(ccdCase, authorisation,
            extendedResponseDeadline);

        return docAssemblyService.generateDocument(ccdCase,
            authorisation,
            formPayloadForCoverLetter,
            oconN9FormTemplateID);
    }

    private String getCaseWorkerName(String authorisation) {
        var userDetails = userService.getUserDetails(authorisation);
        return userDetails.getFullName();
    }

    public CCDDocument createOconForm(
        CCDCase ccdCase,
        Claim claim,
        String authorisation,
        LocalDate extendedResponseDeadline,
        boolean disableN9Form
    ) {
        var paperResponseLetter = formForCorrectDefendantType(ccdCase, claim, extendedResponseDeadline, disableN9Form);
        return docAssemblyService.generateDocument(ccdCase,
            authorisation,
            paperResponseLetter.getPayload(),
            paperResponseLetter.getTemplateId());
    }

    private PaperResponseLetter formForCorrectDefendantType(CCDCase ccdCase, Claim claim, LocalDate extendedDeadline,
                                                            boolean disableN9Form) {
        PaperResponseLetter.PaperResponseLetterBuilder paperResponseLetter = PaperResponseLetter.builder();
        CCDPartyType partyType = ccdCase.getRespondents().get(0).getValue().getClaimantProvidedDetail().getType();

        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        CCDApplicant applicant = ccdCase.getApplicants().get(0).getValue();
        CCDParty givenRespondent = respondent.getClaimantProvidedDetail();

        CCDAddress defendantAddress = getDefendantAddress(respondent, givenRespondent);

        CCDAddress claimantAddress = getClaimantAddress(applicant);

        String courtName = getCourtName(partyType, defendantAddress, claimantAddress);

        switch (partyType) {
            case INDIVIDUAL:
                if (FeaturesUtils.isOnlineDQ(claim)) {
                    return paperResponseLetter
                        .templateId(oconFormIndividualWithDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormIndividualWithDQsMapper(ccdCase, extendedDeadline, courtName, disableN9Form))
                        .build();
                } else {
                    return paperResponseLetter
                        .templateId(oconFormIndividualWithoutDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormIndividualWithoutDQsMapper(ccdCase, extendedDeadline, disableN9Form))
                        .build();
                }
            case ORGANISATION:
            case COMPANY:
                if (FeaturesUtils.isOnlineDQ(claim)) {
                    return paperResponseLetter
                        .templateId(oconFormOrganisationWithDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormOrganisationWithDQsMapper(ccdCase, extendedDeadline, courtName, disableN9Form))
                        .build();
                } else {
                    return paperResponseLetter
                        .templateId(oconFormOrganisationWithoutDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormOrganisationWithoutDQsMapper(ccdCase, extendedDeadline, disableN9Form))
                        .build();
                }
            case SOLE_TRADER:
                if (FeaturesUtils.isOnlineDQ(claim)) {
                    return paperResponseLetter
                        .templateId(oconFormSoleTraderWithDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormSoleTraderWithDQsMapper(ccdCase, extendedDeadline, courtName, disableN9Form))
                        .build();
                } else {
                    return paperResponseLetter
                        .templateId(oconFormSoleTraderWithoutDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormSoleTraderWithoutDQsMapper(ccdCase, extendedDeadline, disableN9Form))
                        .build();
                }
            default:
                throw new MappingException();
        }
    }

    private String getCourtName(CCDPartyType partyType, CCDAddress defendantAddress, CCDAddress claimantAddress) {
        return courtFinderApi.findMoneyClaimCourtByPostcode((partyType == CCDPartyType.COMPANY
            || partyType == CCDPartyType.ORGANISATION)
            ? claimantAddress.getPostCode() : defendantAddress.getPostCode())
            .stream()
            .map(Court::getName)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No court found"));
    }

    private CCDAddress getClaimantAddress(CCDApplicant applicant) {
        return applicant.getPartyDetail().getPrimaryAddress();
    }

    private CCDAddress getDefendantAddress(CCDRespondent respondent, CCDParty givenRespondent) {

        return respondent.getPartyDetail() != null && respondent.getPartyDetail().getPrimaryAddress() != null
            ? respondent.getPartyDetail().getPrimaryAddress() : givenRespondent.getPrimaryAddress();
    }

    public CCDCase addCoverLetterToCaseWithDocuments(
        CCDCase ccdCase,
        Claim claim,
        CCDDocument coverLetter,
        String authorisation
    ) {
        var documentName = String.format(LETTER_NAME, claim.getReferenceNumber());
        return generalLetterService.attachGeneralLetterToCase(ccdCase, coverLetter, documentName, authorisation);
    }
}
