package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;

import java.util.stream.Collectors;

@Component
public class ClaimDocumentCollectionMapper implements BuilderMapper<CCDCase, ClaimDocumentCollection, CCDCase.CCDCaseBuilder>  {

    private final ClaimDocumentMapper claimDocumentMapper;

    @Autowired
    public ClaimDocumentCollectionMapper(ClaimDocumentMapper claimDocumentMapper) {
        this.claimDocumentMapper = claimDocumentMapper;
    }

    @Override
    public void to(ClaimDocumentCollection claimDocumentCollection, CCDCase.CCDCaseBuilder builder) {
        if (claimDocumentCollection == null || claimDocumentCollection.getClaimDocuments() == null || claimDocumentCollection.getClaimDocuments().isEmpty()) {
            return;
        }

        builder.caseDocuments(
            claimDocumentCollection
                .getClaimDocuments()
                .stream()
                .map(claimDocumentMapper::to)
                .collect(Collectors.toList())
        );
    }

    @Override
    public ClaimDocumentCollection from(CCDCase ccdCase) {
        if (ccdCase.getCaseDocuments() == null || ccdCase.getCaseDocuments().isEmpty()){
            return null;
        }

        ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
        ccdCase.getCaseDocuments()
            .stream()
            .map(claimDocumentMapper::from)
            .forEach(claimDocumentCollection::addClaimDocument);

        return claimDocumentCollection;
    }
}
