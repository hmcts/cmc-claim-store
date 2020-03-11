package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.domain.models.sampledata.offers.SamplePartyStatement.acceptPartyStatement;
import static uk.gov.hmcts.cmc.domain.models.sampledata.offers.SamplePartyStatement.offerPartyStatement;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DefendantMapperTest {

    @Autowired
    private DefendantMapper mapper;

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenTheirDetailsIsNull() {
        mapper.to(null, SampleClaim.getDefault());
    }

    private final LocalDateTime mediationSettledTime = LocalDateTime.of(2019, 11, 13, 8, 20, 30);

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenClaimIsNull() {
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        mapper.to(theirDetails, null);
    }

    @Test
    public void mapToCCDDefendantWithoutResponseDetails() {

        // Given
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        Claim claim = SampleClaim.claim(SampleClaimData.submittedByClaimantBuilder().build(),
            "previousServiceCaseReference");

        //When
        CCDCollectionElement<CCDRespondent> ccdRespondent = mapper.to(theirDetails, claim);
        CCDRespondent respondent = ccdRespondent.getValue();

        //Then
        assertThat(theirDetails.getId()).isEqualTo(ccdRespondent.getId());

        assertEquals("Response served date is not mapped properly",
            respondent.getServedDate(), claim.getServiceDate());

        assertEquals("Claim response deadline is not mapped properly",
            respondent.getResponseDeadline(), claim.getResponseDeadline());

        assertEquals("Claim response letter holder id is not mapped properly",
            respondent.getLetterHolderId(), claim.getLetterHolderId());

        assertEquals("Claim defendantId is not mapped properly",
            respondent.getDefendantId(), claim.getDefendantId());

        assertEquals("Claim defendant email is not mapped properly",
            respondent.getPartyDetail().getEmailAddress(), claim.getDefendantEmail());

        assertEquals("Claim response more time requested is not mapped properly",
            respondent.getResponseMoreTimeNeededOption().toBoolean(), claim.isMoreTimeRequested());

        assertEquals("The claimantProvidedType should be of organization",
            ORGANISATION, respondent.getClaimantProvidedDetail().getType());
    }

    @Test
    public void mapToCCDDefendantWithResponseDetails() {
        // Given
        TheirDetails theirDetails = SampleTheirDetails.builder().individualDetails();
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation();

        //When
        CCDCollectionElement<CCDRespondent> ccdRespondent = mapper.to(theirDetails, claim);
        CCDRespondent respondent = ccdRespondent.getValue();
        //Then
        assertEquals("Response served date is not mapped properly",
            respondent.getServedDate(), claim.getServiceDate());

        assertEquals("Claim response deadline is not mapped properly",
            respondent.getResponseDeadline(), claim.getResponseDeadline());

        assertEquals("Claim response letter holder id is not mapped properly",
            respondent.getLetterHolderId(), claim.getLetterHolderId());

        assertEquals("Claim defendantId is not mapped properly",
            respondent.getDefendantId(), claim.getDefendantId());

        assertEquals("Claim defendant email is not mapped properly",
            respondent.getPartyDetail().getEmailAddress(), claim.getDefendantEmail());

        assertEquals("Claim response more time requested is not mapped properly",
            respondent.getResponseMoreTimeNeededOption().toBoolean(), claim.isMoreTimeRequested());

        //Verify if the TheirDetails mapper and response mapper are called by assert not null
        assertThat(respondent.getResponseSubmittedOn()).isNotNull();
        assertThat(respondent.getResponseType()).isNotNull();
        assertThat(respondent.getClaimantProvidedDetail()).isNotNull();
        assertThat(respondent.getClaimantProvidedDetail().getType()).isNotNull();

        assertEquals("The mapping for theirDetailsMapper is not done properly",
            INDIVIDUAL, respondent.getPartyDetail().getType());

        assertEquals("The claim response submitted is not mapped properly when response is present",
            respondent.getResponseSubmittedOn(), claim.getRespondedAt());

        assertEquals("The Response mapper is not called / mapped when response is available",
            respondent.getResponseType().name(), claim.getResponse().map(Response::getResponseType)
                .map(ResponseType::name).orElse(null));
    }

    @Test
    public void mapTheirDetailsFromCCDClaimWithNoResponse() {
        //Given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withResponseMoreTimeNeededOption().build();
        Claim.ClaimBuilder claimBuilder = Claim.builder().serviceDate(now());

        //when
        mapper.from(claimBuilder, CCDCollectionElement.<CCDRespondent>builder().value(ccdRespondent).build());
        Claim finalClaim = claimBuilder.build();

        // Then
        assertEquals("Response served date is not mapped properly",
            finalClaim.getServiceDate(), ccdRespondent.getServedDate());

        assertEquals("response deadline is not mapped properly",
            finalClaim.getResponseDeadline(), ccdRespondent.getResponseDeadline());

        assertEquals("Claim response letter holder id is not mapped properly",
            finalClaim.getLetterHolderId(), ccdRespondent.getLetterHolderId());

        assertEquals("Claim defendantId is not mapped properly",
            finalClaim.getDefendantId(), ccdRespondent.getDefendantId());

        assertEquals("Claim defendant email is not mapped properly",
            finalClaim.getDefendantEmail(), ccdRespondent.getPartyDetail().getEmailAddress());

        assertEquals("Claim response more time requested is not mapped properly",
            finalClaim.isMoreTimeRequested(), ccdRespondent.getResponseMoreTimeNeededOption().toBoolean());
    }

    @Test
    public void mapTheirDetailsFromCCDClaimWithNoResponseMoreTimeNeededOption() {
        //Given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withDefault().build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        mapper.from(claimBuilder, CCDCollectionElement.<CCDRespondent>builder().value(ccdRespondent).build());
        Claim finalClaim = claimBuilder.build();

        // Then
        assertThat(finalClaim.isMoreTimeRequested()).as("Claim response more time requested is not mapped properly")
            .isFalse();
    }

    @Test
    public void mapTheirDetailsFromCCDClaimWithResponse() {
        //Given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withResponseMoreTimeNeededOption().build();

        CCDCollectionElement<CCDRespondent> defendant
            = CCDCollectionElement.<CCDRespondent>builder().value(ccdRespondent).build();

        Claim.ClaimBuilder claimBuilder = Claim.builder().issuedOn(now());

        //when
        TheirDetails party = mapper.from(claimBuilder, defendant);
        Claim finalClaim = claimBuilder.build();

        // Then
        assertThat(party).isInstanceOf(IndividualDetails.class);

        assertEquals("Response served date is not mapped properly",
            finalClaim.getServiceDate(), ccdRespondent.getServedDate());

        assertEquals("Response deadline is not mapped properly",
            finalClaim.getResponseDeadline(), ccdRespondent.getResponseDeadline());

        assertEquals("Claim response letter holder id is not mapped properly",
            finalClaim.getLetterHolderId(), ccdRespondent.getLetterHolderId());

        assertEquals("Claim defendantId is not mapped properly",
            finalClaim.getDefendantId(), ccdRespondent.getDefendantId());

        assertEquals("Claim defendant email is not mapped properly",
            finalClaim.getDefendantEmail(), ccdRespondent.getPartyDetail().getEmailAddress());

        assertEquals("Claim response more time requested is not mapped properly",
            finalClaim.isMoreTimeRequested(), ccdRespondent.getResponseMoreTimeNeededOption().toBoolean());
    }

    @Test
    public void mapCountyCourtJudgmentToCCDDefendant() {
        //Given
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        Claim claimWithCCJ = SampleClaim.withDefaultCountyCourtJudgment();
        CountyCourtJudgment countyCourtJudgment = claimWithCCJ.getCountyCourtJudgment();

        //When
        CCDCollectionElement<CCDRespondent> defendant = mapper.to(theirDetails, claimWithCCJ);
        CCDRespondent respondent = defendant.getValue();
        //Then
        CCDCountyCourtJudgment ccdCountyCourtJudgment = respondent.getCountyCourtJudgmentRequest();
        assertNotNull(ccdCountyCourtJudgment);
        assertEquals(ccdCountyCourtJudgment.getType().name(), countyCourtJudgment.getCcjType().name());
        assertEquals(ccdCountyCourtJudgment.getRequestedDate(), claimWithCCJ.getCountyCourtJudgmentRequestedAt());
    }

    @Test
    public void mapPaidInFullToCCDDefendant() {
        //Given
        TheirDetails theirDetails = SampleTheirDetails.builder().individualDetails();
        LocalDate moneyReceivedOn = now();
        Claim claimWithPaidInFull = SampleClaim.builder().withMoneyReceivedOn(moneyReceivedOn).build();

        //When
        CCDCollectionElement<CCDRespondent> ccdRespondent = mapper.to(theirDetails, claimWithPaidInFull);
        CCDRespondent value = ccdRespondent.getValue();

        //Then
        assertNotNull(value.getPaidInFullDate());
        assertEquals(moneyReceivedOn, value.getPaidInFullDate());
    }

    @Test
    public void mapPaidInFullFromCCDDefendant() {
        //Given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withPaidInFull(now()).build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        mapper.from(claimBuilder, CCDCollectionElement.<CCDRespondent>builder().value(ccdRespondent).build());
        Claim claim = claimBuilder.build();

        //Then
        assertTrue(claim.getMoneyReceivedOn().isPresent());
        assertEquals(ccdRespondent.getPaidInFullDate(), claim.getMoneyReceivedOn()
            .orElseThrow(() -> new AssertionError("Missing money received date")));
    }

    @Test
    public void mapToCCDDefendantWithNullSettlement() {
        //Given
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        Claim claimWithCCJ = SampleClaim.getWithSettlement(null);

        //When
        CCDCollectionElement<CCDRespondent> ccdRespondent = mapper.to(theirDetails, claimWithCCJ);
        CCDRespondent value = ccdRespondent.getValue();

        //Then
        assertNull(value.getSettlementPartyStatements());
    }

    @Test
    public void mapToCCDDefendantWithSettlements() {
        //Given
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        Claim claimWithSettlement = SampleClaim
            .getWithSettlement(SampleSettlement.builder()
                .withPartyStatements(offerPartyStatement, acceptPartyStatement).build());

        final LocalDateTime settlementReachedAt = claimWithSettlement.getSettlementReachedAt();

        //When
        CCDCollectionElement<CCDRespondent> collectionElement = mapper.to(theirDetails, claimWithSettlement);
        CCDRespondent ccdRespondent = collectionElement.getValue();

        //Then
        assertNotNull(ccdRespondent.getSettlementPartyStatements());
        assertNotNull(ccdRespondent.getSettlementReachedAt());
        assertThat(ccdRespondent.getSettlementPartyStatements()).hasSize(2);
        assertEquals(settlementReachedAt, ccdRespondent.getSettlementReachedAt());
    }

    @Test
    public void mapToCCDDefendantWithMediationFailure() {
        //Given
        final String failureReason = "Mediation Failed";
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        Claim claimWithSettlement = SampleClaim
            .getWithSettlement(SampleSettlement.builder()
                .withPartyStatements(offerPartyStatement, acceptPartyStatement).build())
            .toBuilder()
            .failedMediationReason(failureReason)
            .mediationSettlementReachedAt(mediationSettledTime)
            .build();

        //When
        CCDCollectionElement<CCDRespondent> collectionElement = mapper.to(theirDetails, claimWithSettlement);
        CCDRespondent ccdRespondent = collectionElement.getValue();

        //Then
        assertNotNull(ccdRespondent.getMediationFailedReason());
        assertNotNull(ccdRespondent.getMediationSettlementReachedAt());
        assertThat(ccdRespondent.getMediationFailedReason()).isEqualTo(failureReason);
        assertThat(ccdRespondent.getMediationSettlementReachedAt()).isEqualTo(mediationSettledTime);

    }

    @Test
    public void mapFromCCDDefendantWithNoSettlementDetails() {
        //Given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withResponseMoreTimeNeededOption().build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        mapper.from(claimBuilder, CCDCollectionElement.<CCDRespondent>builder().value(ccdRespondent).build());

        //Then
        assertNull(ccdRespondent.getSettlementReachedAt());
        assertNull(ccdRespondent.getSettlementPartyStatements());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void mapFromCCDDefendantWithSettlements() {
        //Given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withPartyStatements().build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        mapper.from(claimBuilder, CCDCollectionElement.<CCDRespondent>builder().value(ccdRespondent).build());
        Claim finalClaim = claimBuilder.build();

        // Then
        assertNotNull(finalClaim.getSettlementReachedAt());
        assertNotNull(finalClaim.getSettlement());
        assertThat(finalClaim.getSettlement().get().getPartyStatements().size()).isEqualTo(3);
    }

    @Test
    public void mapFromCCDDefendantWithMediationFailureReason() {
        //Given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withMediationFailureReason().build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        mapper.from(claimBuilder, CCDCollectionElement.<CCDRespondent>builder().value(ccdRespondent).build());
        Claim finalClaim = claimBuilder.build();

        // Then
        assertEquals(finalClaim.getFailedMediationReason(), Optional.of("Defendant phone died while mediation call"));

    }

    @Test
    public void mapFromCCDDefendantWithMediationSettlementReached() {
        //Given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withMediationAgreementDate(mediationSettledTime).build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        mapper.from(claimBuilder, CCDCollectionElement.<CCDRespondent>builder().value(ccdRespondent).build());
        Claim finalClaim = claimBuilder.build();

        // Then
        assertEquals(mediationSettledTime, finalClaim.getMediationSettlementReachedAt()
            .orElseThrow(() -> new AssertionError("Expected mediation settled time but got null")));

    }
}
