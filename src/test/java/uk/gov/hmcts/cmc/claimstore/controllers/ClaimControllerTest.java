package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.ioc.CreatePaymentResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleReviewOrder;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.LETTER_HOLDER_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

@RunWith(MockitoJUnitRunner.class)
public class ClaimControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final List<String> FEATURES = singletonList("admissions");

    private ClaimController claimController;

    @Mock
    private ClaimService claimService;

    @Before
    public void setup() {
        claimController = new ClaimController(claimService);
    }

    @Test
    public void shouldSaveClaimInRepository() {
        //given
        ClaimData input = SampleClaimData.validDefaults();
        when(claimService
            .saveClaim(
                USER_ID, input, AUTHORISATION, FEATURES))
            .thenReturn(CLAIM);

        //when
        Claim output = claimController.save(input, USER_ID, AUTHORISATION, FEATURES);

        //then
        assertThat(output).isEqualTo(CLAIM);
    }

    @Test
    public void shouldSaveLegalRepClaimInRepository() {
        //given
        ClaimData input = SampleClaimData.validDefaults();
        when(claimService.saveRepresentedClaim(eq(USER_ID),
            eq(input), eq(AUTHORISATION))
        )
            .thenReturn(CLAIM);

        //when
        Claim output = claimController.saveLegalRepresentedClaim(input, USER_ID, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(CLAIM);
    }

    @Test
    public void shouldReturnClaimFromRepositoryForClaimantId() {
        //given
        when(claimService.getClaimBySubmitterId(eq(USER_ID), eq(AUTHORISATION)))
            .thenReturn(singletonList(CLAIM));

        //when
        List<Claim> output = claimController.getBySubmitterId(USER_ID, AUTHORISATION);

        //then
        assertThat(output.get(0)).isEqualTo(CLAIM);
    }

    @Test
    public void shouldReturnClaimFromRepositoryForLetterHolderId() {
        //given
        when(claimService.getClaimByLetterHolderId(eq(LETTER_HOLDER_ID), any())).thenReturn(CLAIM);

        //when
        Claim output = claimController.getByLetterHolderId(LETTER_HOLDER_ID, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(CLAIM);
    }

    @Test
    public void shouldSaveReviewOrder() {
        //given
        ReviewOrder reviewOrder = SampleReviewOrder.getDefault();
        Claim claim = SampleClaim.builder().withReviewOrder(reviewOrder).build();
        when(claimService.saveReviewOrder(eq(EXTERNAL_ID), eq(reviewOrder), eq(AUTHORISATION)))
            .thenReturn(claim);

        //when
        Claim output = claimController.saveReviewOrder(EXTERNAL_ID, reviewOrder, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(claim);
    }

    @Test
    public void shouldInitiatePaymentForCitizen() {
        //given
        ClaimData input = SampleClaimData.validDefaults();

        CreatePaymentResponse response = CreatePaymentResponse.builder().build();
        when(claimService.initiatePayment(AUTHORISATION, input))
            .thenReturn(response);

        //when
        CreatePaymentResponse output = claimController.initiatePayment(input, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(response);
    }

    @Test
    public void shouldResumePaymentForCitizen() {
        //given
        ClaimData claimData = SampleClaimData.builder().build();
        CreatePaymentResponse expectedResponse =
            CreatePaymentResponse.builder().nextUrl("http://next.url").build();
        when(claimService.resumePayment(AUTHORISATION, claimData))
            .thenReturn(expectedResponse);

        //when
        CreatePaymentResponse output = claimController.resumePayment(claimData, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldCreateClaimForCitizen() {
        //given
        ClaimData claimData = SampleClaimData.builder().build();
        Claim expectedResponse = Claim.builder().claimData(claimData).build();
        when(claimService.createCitizenClaim(AUTHORISATION, claimData, FEATURES))
            .thenReturn(expectedResponse);

        //when
        Claim output = claimController.createClaim(claimData, AUTHORISATION, FEATURES);

        //then
        assertThat(output).isEqualTo(expectedResponse);
    }
}
