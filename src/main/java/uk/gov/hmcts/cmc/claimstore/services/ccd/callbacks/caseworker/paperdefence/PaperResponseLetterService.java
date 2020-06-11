package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
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
    private final PaperDefenceLetterBodyMapper paperDefenceLetterBodyMapper;
    private final DocAssemblyService docAssemblyService;
    private final UserService userService;
    private final GeneralLetterService generalLetterService;

    @Autowired
    public PaperResponseLetterService(
        @Value("${doc_assembly.oconFormIndividualWithDQs}") String oconFormIndividualWithDQs,
        @Value("${doc_assembly.oconFormSoleTraderWithDQs}") String oconFormSoleTraderWithDQs,
        @Value("${doc_assembly.oconFormOrganisationWithDQs}") String oconFormOrganisationWithDQs,
        @Value("${doc_assembly.oconFormIndividualWithoutDQs}") String oconFormIndividualWithoutDQs,
        @Value("${doc_assembly.oconFormSoleTraderWithoutDQs}") String oconFormSoleTraderWithoutDQs,
        @Value("${doc_assembly.oconFormOrganisationWithoutDQs}") String oconFormOrganisationWithoutDQs,
        @Value("${doc_assembly.paperDefenceCoverLetterTemplateID}") String paperDefenceCoverLetterTemplateID,
        PaperDefenceLetterBodyMapper paperDefenceLetterBodyMapper,
        DocAssemblyService docAssemblyService,
        UserService userService,
        GeneralLetterService generalLetterService
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
    }

    public CCDDocument createCoverLetter(CCDCase ccdCase, String authorisation, LocalDate extendedResponseDeadline) {
        DocAssemblyTemplateBody formPayloadForCoverLetter =
            paperDefenceLetterBodyMapper.coverLetterTemplateMapper(
                ccdCase, getCaseWorkerName(authorisation), extendedResponseDeadline);

        return docAssemblyService.generateDocument(authorisation,
            formPayloadForCoverLetter,
            paperDefenceCoverLetterTemplateID);
    }

    private String getCaseWorkerName(String authorisation) {
        var userDetails = userService.getUserDetails(authorisation);
        return userDetails.getFullName();
    }

    public CCDDocument createOconForm(
        CCDCase ccdCase,
        Claim claim,
        String authorisation,
        LocalDate extendedResponseDeadline
    ) {
        var paperResponseLetter = formForCorrectDefendantType(ccdCase, claim, extendedResponseDeadline);
        return docAssemblyService.generateDocument(authorisation,
            paperResponseLetter.payload,
            paperResponseLetter.templateId);
    }

    private PaperResponseLetter formForCorrectDefendantType(CCDCase ccdCase, Claim claim, LocalDate extendedDeadline) {
        PaperResponseLetter.PaperResponseLetterBuilder paperResponseLetter = PaperResponseLetter.builder();
        CCDPartyType partyType = ccdCase.getRespondents().get(0).getValue().getPartyDetail().getType();
        switch (partyType) {
            case INDIVIDUAL:
                if (FeaturesUtils.isOnlineDQ(claim)) {
                    return paperResponseLetter
                        .templateId(oconFormIndividualWithDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormIndividualWithDQsMapper(ccdCase, extendedDeadline))
                        .build();
                } else {
                    return paperResponseLetter
                        .templateId(oconFormIndividualWithoutDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormIndividualWithoutDQsMapper(ccdCase, extendedDeadline))
                        .build();
                }
            case ORGANISATION:
            case COMPANY:
                if (FeaturesUtils.isOnlineDQ(claim)) {
                    return paperResponseLetter
                        .templateId(oconFormOrganisationWithDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormOrganisationWithDQsMapper(ccdCase, extendedDeadline))
                        .build();
                } else {
                    return paperResponseLetter
                        .templateId(oconFormOrganisationWithoutDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormOrganisationWithoutDQsMapper(ccdCase, extendedDeadline))
                        .build();
                }
            case SOLE_TRADER:
                if (FeaturesUtils.isOnlineDQ(claim)) {
                    return paperResponseLetter
                        .templateId(oconFormSoleTraderWithDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormSoleTraderWithDQsMapper(ccdCase, extendedDeadline))
                        .build();
                } else {
                    return paperResponseLetter
                        .templateId(oconFormSoleTraderWithoutDQs)
                        .payload(paperDefenceLetterBodyMapper
                            .oconFormSoleTraderWithoutDQsMapper(ccdCase, extendedDeadline))
                        .build();
                }
            default:
                throw new MappingException();
        }
    }

    public CCDCase addCoverLetterToCaseWithDocuments(
        CCDCase ccdCase,
        Claim claim,
        CCDDocument coverLetter,
        String authorisation
    ) {
        String documentName = String.format(LETTER_NAME, claim.getReferenceNumber());
        return generalLetterService.attachGeneralLetterToCase(ccdCase, coverLetter, documentName, authorisation);
    }
}
