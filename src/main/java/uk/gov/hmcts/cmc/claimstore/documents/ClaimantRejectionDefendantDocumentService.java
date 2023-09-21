package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.ccd.util.PartyNameUtils.getPartyNameFor;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.claimstore.utils.Preconditions.requireNonBlank;

@Service
public class ClaimantRejectionDefendantDocumentService {

    private final String defendantOconN9xClaimantMediation;
    private final DocAssemblyService docAssemblyService;
    private final String caseTypeId;
    private final String jurisdictionId;
    private final CaseMapper caseMapper;

    @Autowired
    ClaimantRejectionDefendantDocumentService(@Value("${doc_assembly.defendantPinLetterTemplateID}")
                                                  String defendantOconN9xClaimantMediation,
                                              @Value("${ocmc.caseTypeId}") String caseTypeId,
                                              @Value("${ocmc.jurisdictionId}") String jurisdictionId,
                                              DocAssemblyService docAssemblyService,
                                              CaseMapper caseMapper
                                              ){
        this.defendantOconN9xClaimantMediation = defendantOconN9xClaimantMediation;
        this.docAssemblyService = docAssemblyService;
        this.caseTypeId = caseTypeId;
        this.jurisdictionId = jurisdictionId;
        this.caseMapper = caseMapper;
    }

    public CCDDocument createClaimantRejectionDocument(Claim claim, String authorisation) {
        DocAssemblyTemplateBody formPayloadForPinLetter = defendantDetailsTemplateMapper(claim);
        CCDCase ccdCase = caseMapper.to(claim);
        return docAssemblyService.generateDocument(ccdCase,
            authorisation,
            formPayloadForPinLetter,
            defendantOconN9xClaimantMediation,
            caseTypeId, jurisdictionId);
    }

    public DocAssemblyTemplateBody defendantDetailsTemplateMapper(Claim claim
    ) {
        requireNonNull(claim);
        return DocAssemblyTemplateBody.builder()
            .build();
    }
}
