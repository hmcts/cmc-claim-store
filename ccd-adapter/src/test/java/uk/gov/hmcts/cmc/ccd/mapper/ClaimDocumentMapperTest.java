package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.net.URI;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimDocumentMapperTest {

    @Autowired
    private ClaimDocumentMapper claimDocumentMapper;

    private ClaimDocument claimDocument;

    private static final String NAME = "foo";

    private static final String URL = "www.google.com";

    private LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    private LocalDateTime now = LocalDateTime.now();

    private static final String CREATED_BY = "bar";

    @Before
    public void setUp() {
        claimDocument = ClaimDocument.builder()
            .documentName(NAME)
            .documentManagementUrl(URI.create(URL))
            .documentType(ClaimDocumentType.CLAIM_ISSUE_RECEIPT)
            .authoredDatetime(now)
            .createdDatetime(yesterday)
            .createdBy(CREATED_BY)
            .build();
    }

    @Test
    public void mapTo() {
        CCDCollectionElement<CCDClaimDocument> ccdCollectionElement = claimDocumentMapper.to(claimDocument);

        assertEquals(claimDocument.getDocumentName(), ccdCollectionElement.getValue().getDocumentName());
        assertEquals(claimDocument.getDocumentManagementUrl(), ccdCollectionElement.getValue().getDocumentLink());
        assertEquals(CCDClaimDocumentType.CLAIM_ISSUE_RECEIPT.getTypeName(),
            ccdCollectionElement.getValue().getDocumentType());
        assertEquals(claimDocument.getAuthoredDatetime(), ccdCollectionElement.getValue().getAuthoredDatetime());
        assertEquals(claimDocument.getCreatedDatetime(), ccdCollectionElement.getValue().getCreatedDatetime());
        assertEquals(claimDocument.getCreatedBy(), ccdCollectionElement.getValue().getCreatedBy());
    }

    @Test
    public void mapFrom() {
        CCDCollectionElement<CCDClaimDocument> ccdCollectionElement = claimDocumentMapper.to(claimDocument);
        ClaimDocument claimDocument = claimDocumentMapper.from(ccdCollectionElement);
        CCDClaimDocument ccdClaimDocument = ccdCollectionElement.getValue();

        assertEquals(ccdClaimDocument.getDocumentName(), claimDocument.getDocumentName());
        assertEquals(ccdClaimDocument.getDocumentLink(), claimDocument.getDocumentManagementUrl());
        assertEquals(ClaimDocumentType.CLAIM_ISSUE_RECEIPT, claimDocument.getDocumentType());
        assertEquals(ccdClaimDocument.getAuthoredDatetime(), ccdClaimDocument.getAuthoredDatetime());
        assertEquals(ccdClaimDocument.getCreatedDatetime(), ccdClaimDocument.getCreatedDatetime());
        assertEquals(ccdClaimDocument.getCreatedBy(), ccdClaimDocument.getCreatedBy());
    }
}
