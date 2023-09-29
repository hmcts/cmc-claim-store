package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.AddressMapper;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

@Service
public class ClaimantRejectionDefendantDocumentService {

    private final String defendantOconN9xClaimantMediation;
    private final DocAssemblyService docAssemblyService;
    private final String caseTypeId;
    private final String jurisdictionId;
    private final CaseMapper caseMapper;
    private final AddressMapper addressMapper;

    @Autowired
    ClaimantRejectionDefendantDocumentService(@Value("${doc_assembly.defendantOconN9xClaimantMediation}")
                                                  String defendantOconN9xClaimantMediation,
                                              @Value("${ocmc.caseTypeId}") String caseTypeId,
                                              @Value("${ocmc.jurisdictionId}") String jurisdictionId,
                                              DocAssemblyService docAssemblyService,
                                              CaseMapper caseMapper,
                                              AddressMapper addressMapper) {
        this.defendantOconN9xClaimantMediation = defendantOconN9xClaimantMediation;
        this.docAssemblyService = docAssemblyService;
        this.caseTypeId = caseTypeId;
        this.jurisdictionId = jurisdictionId;
        this.caseMapper = caseMapper;
        this.addressMapper = addressMapper;
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
            .partyName(claim.getClaimData().getDefendant().getName())
            .partyAddress(addressMapper.to(claim.getClaimData().getDefendant().getAddress()))
            .referenceNumber(claim.getReferenceNumber())
            .claimantName(claim.getClaimData().getClaimant().getName())
            .currentDate(LocalDate.now())
            .build();
    }
}
