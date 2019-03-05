package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Component
public class ClaimDocumentCollectionMapper
    implements BuilderMapper<CCDCase, ClaimDocumentCollection, CCDCase.CCDCaseBuilder>  {

    private final ClaimDocumentMapper claimDocumentMapper;

    @Autowired
    public ClaimDocumentCollectionMapper(ClaimDocumentMapper claimDocumentMapper) {
        this.claimDocumentMapper = claimDocumentMapper;
    }

    @Override
    public void to(ClaimDocumentCollection claimDocumentCollection, CCDCase.CCDCaseBuilder builder) {
        if (claimDocumentCollection == null
            || claimDocumentCollection.getClaimDocuments() == null
            || claimDocumentCollection.getClaimDocuments().isEmpty()) {
            return;
        }

        /**
         * TODO claimDocumentCollection contains duplicate items
         */
        Collection<ClaimDocument> claimDocumentCollection2 = claimDocumentCollection
            .getClaimDocuments()
            .stream()
            .collect(toMap(ClaimDocument::getDocumentName, p -> p, (p, q) -> p)).values();

        builder.caseDocuments(
            claimDocumentCollection2
                .stream()
                .map(claimDocumentMapper::to)
                .collect(Collectors.toList())
        );
    }

    @Override
    public ClaimDocumentCollection from(CCDCase ccdCase) {
        if (ccdCase.getCaseDocuments() == null || ccdCase.getCaseDocuments().isEmpty()) {
            return null;
        }

        ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
        ccdCase.getCaseDocuments()
            .stream()
            .map(claimDocumentMapper::from)
            .forEach(claimDocumentCollection::addClaimDocument);

        return claimDocumentCollection;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
