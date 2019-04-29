package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ClaimDocumentCollectionMapper {

    private final ClaimDocumentMapper claimDocumentMapper;

    @Autowired
    public ClaimDocumentCollectionMapper(ClaimDocumentMapper claimDocumentMapper) {
        this.claimDocumentMapper = claimDocumentMapper;
    }

    public void to(ClaimDocumentCollection claimDocumentCollection, CCDCase.CCDCaseBuilder builder) {
        if (claimDocumentCollection == null
            || claimDocumentCollection.getClaimDocuments() == null
            || claimDocumentCollection.getClaimDocuments().isEmpty()) {
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

    public void from(CCDCase ccdCase, Claim.ClaimBuilder builder) {
        Objects.requireNonNull(ccdCase, "ccdCase must not be null");

        if (CollectionUtils.isEmpty(ccdCase.getCaseDocuments())) {
            return;
        }

        ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();

        ccdCase.getCaseDocuments()
            .stream()
            .map(claimDocumentMapper::from)
            .forEach(claimDocumentCollection::addClaimDocument);

        builder.claimDocumentCollection(claimDocumentCollection);
    }
}
