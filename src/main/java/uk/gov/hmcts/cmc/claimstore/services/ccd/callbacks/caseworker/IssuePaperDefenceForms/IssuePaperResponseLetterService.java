package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.IssuePaperDefenceForms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class IssuePaperResponseLetterService {
    private final String oconFormIndividualWithDQs;
    private final String oconFormSoleTraderWithDQs;
    private final String oconFormOrganisationWithDQs;
    private final String oconFormIndividualWithoutDQs;
    private final String oconFormSoleTraderWithoutDQs;
    private final String oconFormOrganisationWithoutDQs;
    private final String paperDefenceCoverLetterTemplateID;
    private final PaperDefenceLetterBodyMapper paperDefenceLetterBodyMapper;
    private final DocAssemblyService docAssemblyService;
    private PaperResponseLetter paperResponseLetter;

    @Autowired
    public IssuePaperResponseLetterService(
            @Value("${doc_assembly.oconFormIndividualWithDQs}") String oconFormIndividualWithDQs,
            @Value("${doc_assembly.oconFormSoleTraderWithDQs}") String oconFormSoleTraderWithDQs,
            @Value("${doc_assembly.oconFormOrganisationWithDQs}") String oconFormOrganisationWithDQs,
            @Value("${doc_assembly.oconFormIndividualWithoutDQs}") String oconFormIndividualWithoutDQs,
            @Value("${doc_assembly.oconFormSoleTraderWithoutDQs}") String oconFormSoleTraderWithoutDQs,
            @Value("${doc_assembly.oconFormOrganisationWithoutDQs}") String oconFormOrganisationWithoutDQs,
            @Value("${doc_assembly.paperDefenceCoverLetterTemplateID}") String paperDefenceCoverLetterTemplateID,
            PaperDefenceLetterBodyMapper paperDefenceLetterBodyMapper,
            DocAssemblyService docAssemblyService,
            PaperResponseLetter paperResponseLetter
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
        this.paperResponseLetter = paperResponseLetter;
    }

    public CCDDocument createCoverLetter(CCDCase ccdCase, String caseworkerName, String authorisation) {
        DocAssemblyTemplateBody formPayloadForCoverLetter =
                paperDefenceLetterBodyMapper.coverLetterTemplateMapper(
                        ccdCase, caseworkerName);

        return docAssemblyService.generateDocument(authorisation,
                formPayloadForCoverLetter,
                paperDefenceCoverLetterTemplateID);
    }

    public CCDDocument createOconForm(CCDCase ccdCase, Claim claim, String authorisation) {
        formForCorrectDefendantType(ccdCase, claim);
        return docAssemblyService.generateDocument(authorisation,
                paperResponseLetter.payload,
                paperResponseLetter.templateId);
    }

    private void formForCorrectDefendantType(CCDCase ccdCase, Claim claim) {
        CCDPartyType partyType = ccdCase.getRespondents().get(0).getValue().getPartyDetail().getType();
        switch (partyType) {
            case INDIVIDUAL:
                if(FeaturesUtils.isOnlineDQ(claim)){
                    paperResponseLetter.toBuilder()
                            .templateId(oconFormIndividualWithDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormIndividualWithDQsTemplateMapper(ccdCase));
                } else {
                    paperResponseLetter.toBuilder()
                            .templateId(oconFormIndividualWithoutDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormIndividualWithoutDQsTemplateMapper(ccdCase));
                }
            case ORGANISATION:
            case COMPANY:
                if(FeaturesUtils.isOnlineDQ(claim)){
                    paperResponseLetter.toBuilder()
                            .templateId(oconFormOrganisationWithDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormWithBusinessNameWithDQsTemplateMapper(ccdCase));
                } else {
                    paperResponseLetter.toBuilder()
                            .templateId(oconFormOrganisationWithoutDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormWithBusinessNameWithoutDQsTemplateMapper(ccdCase));
                }
            case SOLE_TRADER:
                if(FeaturesUtils.isOnlineDQ(claim)){
                    paperResponseLetter.toBuilder()
                            .templateId(oconFormSoleTraderWithDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormWithBusinessNameWithDQsTemplateMapper(ccdCase));
                } else {
                    paperResponseLetter.toBuilder()
                            .templateId(oconFormSoleTraderWithoutDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormWithBusinessNameWithoutDQsTemplateMapper(ccdCase));
                }
            default:
                throw new MappingException();
        }
    }
}
