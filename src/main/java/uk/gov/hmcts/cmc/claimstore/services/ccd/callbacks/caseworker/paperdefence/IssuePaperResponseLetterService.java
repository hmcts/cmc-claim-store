package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;

import java.time.Clock;
import java.time.LocalDateTime;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.OCON_FORM;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class IssuePaperResponseLetterService {

    public static final String LETTER_NAME = "%s-issue-paper-form.pdf)";

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
    private final Clock clock;

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
            UserService userService,
            Clock clock
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
        this.clock = clock;
    }

    public CCDDocument createCoverLetter(CCDCase ccdCase, String authorisation) {
        DocAssemblyTemplateBody formPayloadForCoverLetter =
                paperDefenceLetterBodyMapper.coverLetterTemplateMapper(
                        ccdCase, getCaseWorkerName(authorisation));

        return docAssemblyService.generateDocument(authorisation,
                formPayloadForCoverLetter,
                paperDefenceCoverLetterTemplateID);
    }

    private String getCaseWorkerName(String authorisation) {
        var userDetails = userService.getUserDetails(authorisation);
        return userDetails.getFullName();
    }

    public CCDDocument createOconForm(CCDCase ccdCase, Claim claim, String authorisation) {
        var paperResponseLetter = formForCorrectDefendantType(ccdCase, claim);
        return docAssemblyService.generateDocument(authorisation,
                paperResponseLetter.payload,
                paperResponseLetter.templateId);
    }

    private PaperResponseLetter formForCorrectDefendantType(CCDCase ccdCase, Claim claim) {
        PaperResponseLetter.PaperResponseLetterBuilder paperResponseLetter = PaperResponseLetter.builder();
        CCDPartyType partyType = ccdCase.getRespondents().get(0).getValue().getPartyDetail().getType();
        switch (partyType) {
            case INDIVIDUAL:
                if(FeaturesUtils.isOnlineDQ(claim)){
                    paperResponseLetter
                            .templateId(oconFormIndividualWithDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormIndividualWithDQsTemplateMapper(ccdCase));
                } else {
                    paperResponseLetter
                            .templateId(oconFormIndividualWithoutDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormIndividualWithoutDQsTemplateMapper(ccdCase));
                }
            case ORGANISATION:
            case COMPANY:
                if(FeaturesUtils.isOnlineDQ(claim)){
                    paperResponseLetter
                            .templateId(oconFormOrganisationWithDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormOrganisationWithDQsTemplateMapper(ccdCase));
                } else {
                    paperResponseLetter
                            .templateId(oconFormOrganisationWithoutDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormOrganisationWithoutDQsTemplateMapper(ccdCase));
                }
            case SOLE_TRADER:
                if(FeaturesUtils.isOnlineDQ(claim)){
                    paperResponseLetter
                            .templateId(oconFormSoleTraderWithDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormSoleTraderWithDQsTemplateMapper(ccdCase));
                } else {
                    paperResponseLetter
                            .templateId(oconFormSoleTraderWithoutDQs)
                            .payload(paperDefenceLetterBodyMapper.oconFormSoleTraderWithoutDQsTemplateMapper(ccdCase));
                }
        }
        return paperResponseLetter.build();
    }

    public CCDCase updateCaseDocumentsWithDefendantLetter(CCDCase ccdCase, Claim claim, CCDDocument coverLetter) {

        CCDCollectionElement<CCDClaimDocument> defendantCoverLetter = CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                        .documentLink(CCDDocument.builder()
                                .documentFileName(coverLetter.getDocumentFileName())
                                .documentUrl(coverLetter.getDocumentUrl())
                                .documentBinaryUrl(coverLetter.getDocumentBinaryUrl())
                                .build())
                        .documentName(String.format(LETTER_NAME, claim.getReferenceNumber()))
                        .createdDatetime(LocalDateTime.now(clock.withZone(UTC_ZONE)))
                        .documentType(OCON_FORM)
                        .build())
                .build();

        return CCDCase.builder()
                .caseDocuments(
                        ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
                        .addAll(ccdCase.getCaseDocuments())
                        .add(defendantCoverLetter)
                        .build())
                .build();
    }
}
