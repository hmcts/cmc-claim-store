package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SamplePartyStatement;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PartyStatementMapperTest {

    @Autowired
    private PartyStatementMapper partyStatementMapper;

    @Test
    public void mapPartyStatementWithoutPaymentIntentionToCCD() {
        //given
        PartyStatement partyStatement = SamplePartyStatement.builder().build();

        //when
        CCDCollectionElement<CCDPartyStatement> ccdPartyStatement = partyStatementMapper.to(partyStatement);

        //then
        assertThat(partyStatement).isEqualTo(ccdPartyStatement.getValue());
        assertThat(partyStatement.getId()).isEqualTo(ccdPartyStatement.getId());
    }

    @Test
    public void mapPartyStatementWithoutOfferToCCD() {
        //given
        List<PartyStatement> partyStatements = Arrays.asList(
            SamplePartyStatement.counterSignPartyStatement,
            SamplePartyStatement.acceptPartyStatement,
            SamplePartyStatement.offerPartyStatement
        );

        //when
        List<CCDCollectionElement<CCDPartyStatement>> ccdPartyStatements = partyStatements.stream()
            .map(partyStatementMapper::to)
            .collect(Collectors.toList());

        //then
        ListIterator<PartyStatement> partyStatementIterator = partyStatements.listIterator();
        ListIterator<CCDCollectionElement<CCDPartyStatement>> ccdPartyStatementIterator
            = ccdPartyStatements.listIterator();

        while (partyStatementIterator.hasNext()) {
            assertThat(partyStatementIterator.next()).isEqualTo(ccdPartyStatementIterator.next().getValue());
        }

    }

    @Test
    public void mapCCDPartyStatementToDomain() {
        //given
        List<CCDCollectionElement<CCDPartyStatement>> ccdPartyStatements = Stream.of(
            SamplePartyStatement.acceptPartyStatement,
            SamplePartyStatement.rejectPartyStatement,
            SamplePartyStatement.counterSignPartyStatement,
            SamplePartyStatement.offerPartyStatement,
            SamplePartyStatement.builder().build())
            .map(partyStatementMapper::to)
            .collect(Collectors.toList());

        //when
        List<PartyStatement> partyStatements = ccdPartyStatements.stream()
            .map(partyStatementMapper::from)
            .collect(Collectors.toList());

        //then
        ListIterator<PartyStatement> partyStatementIterator = partyStatements.listIterator();

        ListIterator<CCDCollectionElement<CCDPartyStatement>> ccdPartyStatementIterator
            = ccdPartyStatements.listIterator();

        while (partyStatementIterator.hasNext()) {
            assertThat(ccdPartyStatementIterator.next().getValue()).isEqualTo(partyStatementIterator.next());
        }
    }

    @Test
    public void mapEmptyCCDPartyStatementToDomain() {
        //given
        CCDPartyStatement ccdPartyStatement = CCDPartyStatement.builder().build();
        String collectionId = UUID.randomUUID().toString();
        CCDCollectionElement<CCDPartyStatement> collectionElement = CCDCollectionElement.<CCDPartyStatement>builder()
            .id(collectionId)
            .value(ccdPartyStatement)
            .build();

        //when
        PartyStatement partyStatement = partyStatementMapper.from(collectionElement);

        //then
        assertThat(ccdPartyStatement).isEqualTo(partyStatement);
        assertThat(partyStatement.getId()).isEqualTo(collectionId);
    }

    @Test
    public void mapCCDPartyStatementWithOfferToDomain() {
        //given
        CCDPartyStatement ccdPartyStatement = SampleCCDPartyStatement.withOffer();
        String collectionId = UUID.randomUUID().toString();
        CCDCollectionElement<CCDPartyStatement> collectionElement = CCDCollectionElement.<CCDPartyStatement>builder()
            .id(collectionId)
            .value(ccdPartyStatement)
            .build();

        //when
        PartyStatement partyStatement = partyStatementMapper.from(collectionElement);

        //then
        assertThat(ccdPartyStatement).isEqualTo(partyStatement);
        assertThat(partyStatement.getId()).isEqualTo(collectionId);
    }

    @Test
    public void mapCCDPartyStatementWithOfferPaymentIntentionToDomain() {
        //given
        CCDPartyStatement ccdPartyStatement = SampleCCDPartyStatement.withPaymentIntention();
        String collectionId = UUID.randomUUID().toString();

        CCDCollectionElement<CCDPartyStatement> collectionElement = CCDCollectionElement.<CCDPartyStatement>builder()
            .id(collectionId)
            .value(ccdPartyStatement)
            .build();

        //when
        PartyStatement partyStatement = partyStatementMapper.from(collectionElement);

        //then
        assertThat(ccdPartyStatement).isEqualTo(partyStatement);
        assertThat(partyStatement.getId()).isEqualTo(collectionId);
    }

}
