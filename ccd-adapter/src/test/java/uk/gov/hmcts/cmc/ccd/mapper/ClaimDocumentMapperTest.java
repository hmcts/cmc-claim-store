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

    private final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    private final LocalDateTime now = LocalDateTime.now();

    private CCDCollectionElement<CCDClaimDocument> ccdCollectionElement;

    @Before
    public void setUp() {
        claimDocument = ClaimDocument.builder()
            .documentName("foo")
            .documentManagementUrl(URI.create("www.google.com"))
            .documentManagementBinaryUrl(URI.create("www.binary-url.com"))
            .documentType(ClaimDocumentType.CLAIM_ISSUE_RECEIPT)
            .authoredDatetime(now)
            .createdDatetime(yesterday)
            .createdBy("bar")
            .size(123)
            .build();

        ccdCollectionElement = claimDocumentMapper.to(claimDocument);
    }

    @Test
    public void mapTo() {
        assertEquals(claimDocument.getDocumentName(), ccdCollectionElement.getValue().getDocumentName());
        assertEquals(claimDocument.getDocumentManagementUrl(),
            URI.create(ccdCollectionElement.getValue().getDocumentLink().getDocumentUrl()));
        assertEquals(claimDocument.getDocumentManagementBinaryUrl().toString(),
            ccdCollectionElement.getValue().getDocumentLink().getDocumentBinaryUrl());
        assertEquals(claimDocument.getDocumentName(),
            ccdCollectionElement.getValue().getDocumentLink().getDocumentFileName());
        assertEquals(CCDClaimDocumentType.CLAIM_ISSUE_RECEIPT, ccdCollectionElement.getValue().getDocumentType());
        assertEquals(claimDocument.getAuthoredDatetime(), ccdCollectionElement.getValue().getAuthoredDatetime());
        assertEquals(claimDocument.getCreatedDatetime(), ccdCollectionElement.getValue().getCreatedDatetime());
        assertEquals(claimDocument.getCreatedBy(), ccdCollectionElement.getValue().getCreatedBy());
        assertEquals(claimDocument.getSize(), ccdCollectionElement.getValue().getSize());
    }

    @Test
    public void mapFrom() {
        ClaimDocument claimDocument = claimDocumentMapper.from(ccdCollectionElement);
        CCDClaimDocument ccdClaimDocument = ccdCollectionElement.getValue();

        assertEquals(URI.create(ccdClaimDocument.getDocumentLink().getDocumentUrl()),
            claimDocument.getDocumentManagementUrl());

        assertEquals(URI.create(ccdClaimDocument.getDocumentLink().getDocumentBinaryUrl()),
            claimDocument.getDocumentManagementBinaryUrl());

        assertEquals(ccdClaimDocument.getDocumentName(), claimDocument.getDocumentName());
        assertEquals(ClaimDocumentType.CLAIM_ISSUE_RECEIPT, claimDocument.getDocumentType());
        assertEquals(ccdClaimDocument.getAuthoredDatetime(), claimDocument.getAuthoredDatetime());
        assertEquals(ccdClaimDocument.getCreatedDatetime(), claimDocument.getCreatedDatetime());
        assertEquals(ccdClaimDocument.getCreatedBy(), claimDocument.getCreatedBy());
        assertEquals(ccdClaimDocument.getSize(), claimDocument.getSize());
    }
}
