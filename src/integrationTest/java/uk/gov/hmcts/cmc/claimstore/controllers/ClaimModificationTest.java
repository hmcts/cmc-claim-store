package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.ioc.CreatePaymentResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESUME_CLAIM_PAYMENT_CITIZEN;

public class ClaimModificationTest extends BaseMockSpringTest {
    private static final String ROOT_PATH = "/claims";

    private static final String AUTHORISATION_TOKEN_CITIZEN = AUTHORISATION_TOKEN + ": citizen";
    private static final UserDetails CITIZEN_DETAILS = SampleUserDetails.builder()
        .withRoles(Role.CITIZEN.getRole())
        .withUserId(SampleClaim.USER_ID).build();
    private static final User CITIZEN = new User(AUTHORISATION_TOKEN, CITIZEN_DETAILS);

    private static final String AUTHORISATION_TOKEN_LEGAL_REP = AUTHORISATION_TOKEN + ": legal-rep";
    private static final UserDetails LEGAL_REP_DETAILS = SampleUserDetails.builder()
        .withRoles(Role.LEGAL_ADVISOR.getRole())
        .withUserId(SampleClaim.USER_ID).build();
    private static final User LEGAL_REP = new User(AUTHORISATION_TOKEN, CITIZEN_DETAILS);
    private static final String RETURN_URL = "http://return.url";

    @MockBean
    protected EmailService emailService;

    @MockBean
    protected CaseRepository caseRepository;

    @Captor
    private ArgumentCaptor<Claim> claimCaptor;

    @Before
    public void setup() {
        given(userService.getUser(AUTHORISATION_TOKEN_CITIZEN)).willReturn(CITIZEN);
        given(userService.getUserDetails(AUTHORISATION_TOKEN_CITIZEN)).willReturn(CITIZEN_DETAILS);

        given(userService.getUser(AUTHORISATION_TOKEN_LEGAL_REP)).willReturn(LEGAL_REP);
        given(userService.getUserDetails(AUTHORISATION_TOKEN_LEGAL_REP)).willReturn(LEGAL_REP_DETAILS);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void testSaveClaim() throws Exception {
        Claim claim = SampleClaim.getDefault();
        ClaimData claimData = claim.getClaimData();
        when(caseRepository.getClaimByExternalId(anyString(), any(User.class)))
            .thenReturn(Optional.empty());
        when(caseRepository.saveClaim(eq(CITIZEN), any(Claim.class)))
            .thenReturn(claim);
        Claim result = jsonMappingHelper.deserializeObjectFrom(
            doPost(AUTHORISATION_TOKEN_CITIZEN, claimData, ROOT_PATH + "/{submitterId}", SampleClaim.USER_ID)
                .andExpect(status().isOk())
                .andReturn(),
            Claim.class);
        assertThat(result)
            .isNotNull()
            .extracting(Claim::getClaimData)
            .isEqualTo(claimData);

        verify(eventProducer)
            .createClaimCreatedEvent(claim, CITIZEN_DETAILS.getFullName(), AUTHORISATION_TOKEN_CITIZEN);
    }

    @Test
    public void testSaveClaimAlreadyExists() throws Exception {
        Claim claim = SampleClaim.getDefault();
        ClaimData claimData = claim.getClaimData();
        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(claim));
        doPost(AUTHORISATION_TOKEN_CITIZEN, claimData, ROOT_PATH + "/{submitterId}", SampleClaim.USER_ID)
            .andExpect(status().isConflict());
    }

    @Test
    public void testSaveClaimUsesFeatures() throws Exception {
        List<String> features = List.of("Feature 1", "Feature 2");
        Claim claim = SampleClaim.getDefaultWithoutResponse().toBuilder()
            .features(features).build();
        ClaimData claimData = claim.getClaimData();
        when(caseRepository.getClaimByExternalId(anyString(), any(User.class)))
            .thenReturn(Optional.empty());
        when(caseRepository.saveClaim(eq(CITIZEN), any(Claim.class)))
            .thenReturn(claim);

        webClient.perform(
            post(ROOT_PATH + "/{submitterId}", SampleClaim.USER_ID)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN_CITIZEN)
                .header("Features", String.join(",", features))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMappingHelper.toJson(claimData)))
            .andExpect(status().isOk());

        verify(caseRepository).saveClaim(eq(CITIZEN), claimCaptor.capture());
        Claim capturedClaim = claimCaptor.getValue();
        assertThat(capturedClaim)
            .isNotNull()
            .extracting(Claim::getFeatures)
            .isEqualTo(features);
    }

    @Test
    public void testSaveLegalRepresentedClaim() throws Exception {
        Claim claim = SampleClaim.getDefaultForLegal();
        ClaimData claimData = claim.getClaimData();

        when(caseRepository.saveRepresentedClaim(eq(LEGAL_REP), any(Claim.class)))
            .thenReturn(claim);

        Claim result = jsonMappingHelper.deserializeObjectFrom(
            doPost(AUTHORISATION_TOKEN_LEGAL_REP, claimData,
                ROOT_PATH + "/{submitterId}/create-legal-rep-claim", SampleClaim.USER_ID)
                .andExpect(status().isOk())
                .andReturn(),
            Claim.class);
        assertThat(result)
            .isNotNull();

    }

    @Test
    public void testInitiatePayment() throws Exception {
        ClaimData claimData = SampleClaimData.validDefaults();
        ClaimData claimDataWithPaymentDetails = claimData.toBuilder()
            .payment(Payment.builder()
                .nextUrl("http://next.url")
                .build())
            .build();
        Claim claimWithPaymentDetails = SampleClaim.getDefaultWithoutResponse().toBuilder()
            .claimData(claimDataWithPaymentDetails)
            .build();

        when(caseRepository.initiatePayment(eq(CITIZEN), any(Claim.class)))
            .thenReturn(claimWithPaymentDetails);

        CreatePaymentResponse result = jsonMappingHelper.deserializeObjectFrom(
            doPost(AUTHORISATION_TOKEN_CITIZEN, claimData, ROOT_PATH + "/initiate-citizen-payment")
                .andExpect(status().isOk())
                .andReturn(),
            CreatePaymentResponse.class);

        verify(caseRepository).initiatePayment(eq(CITIZEN), claimCaptor.capture());
        Claim capturedClaim = claimCaptor.getValue();
        assertThat(capturedClaim)
            .isNotNull()
            .extracting(Claim::getFeatures)
            .isEqualTo(emptyList());

        assertThat(result)
            .isNotNull()
            .extracting(CreatePaymentResponse::getNextUrl)
            .isEqualTo("http://next.url");
    }

    @Test
    public void testInitiatePaymentNoPaymentFound() throws Exception {
        ClaimData claimDataWithoutPayment = SampleClaimData.validDefaults().toBuilder()
            .payment(null).build();
        Claim claimWithoutPayment = SampleClaim.getDefaultWithoutResponse().toBuilder()
            .claimData(claimDataWithoutPayment).build();

        when(caseRepository.initiatePayment(eq(CITIZEN), any(Claim.class)))
            .thenReturn(claimWithoutPayment);

        doPost(AUTHORISATION_TOKEN_CITIZEN, claimDataWithoutPayment, ROOT_PATH + "/initiate-citizen-payment")
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void testResumePayment() throws Exception {
        ClaimData savedClaimData = SampleClaimData.validDefaults().toBuilder()
            .payment(Payment.builder()
                .amount(BigDecimal.TEN)
                .nextUrl("http://next.url")
                .returnUrl(RETURN_URL)
                .reference("blah".repeat(4))
                .status(PaymentStatus.INITIATED)
                .build())
            .build();
        Claim savedClaim = SampleClaim.claim(savedClaimData, SampleClaim.REFERENCE_NUMBER);
        ClaimData postedClaimData = savedClaimData.toBuilder()
            .reason("A different reason")
            .build();
        ClaimData returnedClaimData = postedClaimData.toBuilder()
            .payment(Payment.builder()
                .nextUrl("http://redirect.to.ocmc")
                .returnUrl(RETURN_URL)
                .amount(BigDecimal.TEN)
                .reference("blah".repeat(4))
                .status(PaymentStatus.SUCCESS)
                .build())
            .build();
        Claim claim = SampleClaim.getDefault().toBuilder()
            .claimData(returnedClaimData)
            .build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(savedClaim));
        when(caseRepository.saveCaseEventIOC(eq(CITIZEN), any(Claim.class), eq(RESUME_CLAIM_PAYMENT_CITIZEN)))
            .thenReturn(claim);

        CreatePaymentResponse response = jsonMappingHelper.deserializeObjectFrom(
            doPut(AUTHORISATION_TOKEN_CITIZEN, postedClaimData, ROOT_PATH + "/resume-citizen-payment")
                .andExpect(status().isOk())
                .andReturn(),
            CreatePaymentResponse.class);

        verify(caseRepository).saveCaseEventIOC(eq(CITIZEN), claimCaptor.capture(), eq(RESUME_CLAIM_PAYMENT_CITIZEN));
        Claim capturedClaim = claimCaptor.getValue();
        assertThat(capturedClaim)
            .isNotNull()
            .extracting(Claim::getClaimData)
            .extracting(ClaimData::getReason)
            .isEqualTo("A different reason");

        assertThat(response)
            .isNotNull()
            .extracting(CreatePaymentResponse::getNextUrl)
            .isEqualTo(RETURN_URL);
    }

    @Test
    public void testResumePaymentPaymentNotSuccessful() throws Exception {
        ClaimData savedClaimData = SampleClaimData.validDefaults().toBuilder()
            .payment(Payment.builder()
                .amount(BigDecimal.TEN)
                .nextUrl("http://next.url")
                .reference("blah".repeat(4))
                .status(PaymentStatus.INITIATED)
                .build())
            .build();
        Claim savedClaim = SampleClaim.claim(savedClaimData, SampleClaim.REFERENCE_NUMBER);
        ClaimData postedClaimData = savedClaimData.toBuilder()
            .reason("A different reason")
            .build();
        ClaimData returnedClaimData = postedClaimData.toBuilder()
            .payment(Payment.builder()
                .nextUrl("http://redirect.to.ocmc")
                .amount(BigDecimal.TEN)
                .reference("blah".repeat(4))
                .status(PaymentStatus.DECLINED)
                .build())
            .build();
        Claim claim = SampleClaim.getDefault().toBuilder()
            .claimData(returnedClaimData)
            .build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(savedClaim));
        when(caseRepository.saveCaseEventIOC(eq(CITIZEN), any(Claim.class), eq(RESUME_CLAIM_PAYMENT_CITIZEN)))
            .thenReturn(claim);

        CreatePaymentResponse response = jsonMappingHelper.deserializeObjectFrom(
            doPut(AUTHORISATION_TOKEN_CITIZEN, postedClaimData, ROOT_PATH + "/resume-citizen-payment")
                .andExpect(status().isOk())
                .andReturn(),
            CreatePaymentResponse.class);

        verify(caseRepository).saveCaseEventIOC(eq(CITIZEN), claimCaptor.capture(), eq(RESUME_CLAIM_PAYMENT_CITIZEN));
        Claim capturedClaim = claimCaptor.getValue();
        assertThat(capturedClaim)
            .isNotNull()
            .extracting(Claim::getClaimData)
            .extracting(ClaimData::getReason)
            .isEqualTo("A different reason");

        assertThat(response)
            .isNotNull()
            .extracting(CreatePaymentResponse::getNextUrl)
            .isEqualTo("http://redirect.to.ocmc");
    }

    @Test
    public void testResumePaymentMissingClaim() throws Exception {
        ClaimData claimData = SampleClaimData.validDefaults();
        when(caseRepository.getClaimByExternalId(anyString(), any(User.class)))
            .thenReturn(Optional.empty());

        doPut(AUTHORISATION_TOKEN_CITIZEN, claimData, ROOT_PATH + "/resume-citizen-payment")
            .andExpect(status().isNotFound());
    }

    @Test
    public void testResumePaymentMissingPayment() throws Exception {
        ClaimData claimDataWithoutPayment = SampleClaimData.builder()
            .withPayment(null).build();
        Claim claimWithoutPayment = SampleClaim.getDefaultWithoutResponse().toBuilder()
            .claimData(claimDataWithoutPayment).build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(claimWithoutPayment));
        when(caseRepository.saveCaseEventIOC(CITIZEN, claimWithoutPayment, RESUME_CLAIM_PAYMENT_CITIZEN))
            .thenReturn(claimWithoutPayment);

        doPut(AUTHORISATION_TOKEN_CITIZEN, claimDataWithoutPayment, ROOT_PATH + "/resume-citizen-payment")
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void testCreateCitizenClaim() throws Exception {
        Claim claim = SampleClaim.getDefaultWithoutResponse();
        ClaimData claimData = SampleClaimData.validDefaults();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(claim));
        when(caseRepository.saveCaseEventIOC(eq(CITIZEN), any(Claim.class), eq(CREATE_CITIZEN_CLAIM)))
            .thenReturn(claim);

        Claim result = jsonMappingHelper.deserializeObjectFrom(
            doPut(AUTHORISATION_TOKEN_CITIZEN, claimData, ROOT_PATH + "/create-citizen-claim")
                .andExpect(status().isOk())
                .andReturn(),
            Claim.class);

        assertThat(result)
            .isNotNull();
    }

    @Test
    public void testCreateCitizenClaimMissingClaim() throws Exception {
        ClaimData claimData = SampleClaimData.validDefaults();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.empty());

        doPut(AUTHORISATION_TOKEN_CITIZEN, claimData, ROOT_PATH + "/create-citizen-claim")
            .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateCitizenClaimUsesFeatures() throws Exception {
        List<String> features = List.of("Feature1", "Feature2", "Feature3");
        Claim claim = SampleClaim.getDefaultWithoutResponse();
        ClaimData claimData = SampleClaimData.validDefaults();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(claim));
        when(caseRepository.saveCaseEventIOC(eq(CITIZEN), any(Claim.class), eq(CREATE_CITIZEN_CLAIM)))
            .thenReturn(claim);

        webClient.perform(
            put(ROOT_PATH + "/create-citizen-claim")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN_CITIZEN)
                .header("Features", String.join(",", features))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMappingHelper.toJson(claimData)))
            .andExpect(status().isOk());

        verify(caseRepository).saveCaseEventIOC(eq(CITIZEN), claimCaptor.capture(), eq(CREATE_CITIZEN_CLAIM));

        Claim capturedClaim = claimCaptor.getValue();
        assertThat(capturedClaim)
            .isNotNull()
            .extracting(Claim::getFeatures)
            .isEqualTo(features);
    }

    @Test
    public void testLinkDefendantToClaim() throws Exception {
        doPut(AUTHORISATION_TOKEN, null, ROOT_PATH + "/defendant/link")
            .andExpect(status().isOk());
        verify(caseRepository).linkDefendant(AUTHORISATION_TOKEN, SampleClaim.LETTER_HOLDER_ID);
    }

    @Test
    public void testRequestMoreTimeToRespond() throws Exception {
        Claim claim = SampleClaim.getDefaultWithoutResponse().toBuilder()
            .defendantId(USER_ID)
            .build();
        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(claim));

        Claim result = jsonMappingHelper.deserializeObjectFrom(
            doPost(AUTHORISATION_TOKEN_CITIZEN, null,
                ROOT_PATH + "/{externalId}/request-more-time", SampleClaim.EXTERNAL_ID)
                .andExpect(status().isOk())
                .andReturn(),
            Claim.class);

        verify(caseRepository)
            .requestMoreTimeForResponse(eq(AUTHORISATION_TOKEN_CITIZEN), eq(claim), any(LocalDate.class));

        assertThat(result)
            .isNotNull();
    }

    @Test
    public void testRequestMoreTimeToRespondMissingClaim() throws Exception {
        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.empty());

        doPost(AUTHORISATION_TOKEN_CITIZEN, null,
            ROOT_PATH + "/{externalId}/request-more-time", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testRequestMoreTimeToRespondAlreadyRequested() throws Exception {
        Claim claim = SampleClaim.getDefaultWithoutResponse().toBuilder()
            .defendantId(USER_ID)
            .moreTimeRequested(true)
            .build();
        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(claim));

        doPost(AUTHORISATION_TOKEN_CITIZEN, null,
            ROOT_PATH + "/{externalId}/request-more-time", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isConflict());
    }

    @Test
    public void testPaidInFull() throws Exception {
        Claim claim = SampleClaim.getDefaultWithoutResponse();
        PaidInFull paidInFull = PaidInFull.builder().moneyReceivedOn(LocalDate.now()).build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(claim));

        Claim result = jsonMappingHelper.deserializeObjectFrom(
            doPut(AUTHORISATION_TOKEN_CITIZEN, paidInFull,
                ROOT_PATH + "/{externalId}/paid-in-full", SampleClaim.EXTERNAL_ID)
                .andExpect(status().isOk())
                .andReturn(),
            Claim.class);

        verify(caseRepository).paidInFull(claim, paidInFull, AUTHORISATION_TOKEN_CITIZEN);
        verify(eventProducer).createPaidInFullEvent(claim);

        assertThat(result)
            .isNotNull();
    }

    @Test
    public void testPaidInFullClaimNotFound() throws Exception {
        PaidInFull paidInFull = PaidInFull.builder().moneyReceivedOn(LocalDate.now()).build();

        when(caseRepository.getClaimByExternalId(anyString(), any(User.class)))
            .thenReturn(Optional.empty());

        doPut(AUTHORISATION_TOKEN_CITIZEN, paidInFull,
            ROOT_PATH + "/{externalId}/paid-in-full", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testPaidInFullMoneyAlreadyReceived() throws Exception {
        Claim claim = SampleClaim.getDefaultWithoutResponse().toBuilder()
            .moneyReceivedOn(LocalDate.now().minus(7, ChronoUnit.DAYS)).build();
        PaidInFull paidInFull = PaidInFull.builder().moneyReceivedOn(LocalDate.now()).build();

        when(caseRepository.getClaimByExternalId(anyString(), any(User.class)))
            .thenReturn(Optional.of(claim));

        doPut(AUTHORISATION_TOKEN_CITIZEN, paidInFull,
            ROOT_PATH + "/{externalId}/paid-in-full", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isConflict());
    }

    @Test
    public void testSaveReviewOrder() throws Exception {
        Claim claim = SampleClaim.getDefault();
        ReviewOrder reviewOrder = ReviewOrder.builder()
            .reason("blah".repeat(4))
            .requestedAt(LocalDateTime.now())
            .requestedBy(ReviewOrder.RequestedBy.CLAIMANT).build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(claim));
        when(caseRepository.saveReviewOrder(claim.getId(), reviewOrder, AUTHORISATION_TOKEN_CITIZEN))
            .thenReturn(claim);

        Claim result = jsonMappingHelper.deserializeObjectFrom(
            doPut(AUTHORISATION_TOKEN_CITIZEN, reviewOrder,
                ROOT_PATH + "/{externalId}/review-order", SampleClaim.EXTERNAL_ID)
                .andExpect(status().isOk())
                .andReturn(),
            Claim.class);

        verify(caseRepository)
            .saveReviewOrder(claim.getId(), reviewOrder, AUTHORISATION_TOKEN_CITIZEN);
        verify(eventProducer)
            .createReviewOrderEvent(AUTHORISATION_TOKEN_CITIZEN, claim);

        assertThat(result)
            .isNotNull();
    }

    @Test
    public void testSaveReviewOrderMissingClaim() throws Exception {
        ReviewOrder reviewOrder = ReviewOrder.builder()
            .reason("blah".repeat(4))
            .requestedAt(LocalDateTime.now())
            .requestedBy(ReviewOrder.RequestedBy.CLAIMANT).build();
        when(caseRepository.getClaimByExternalId(anyString(), any(User.class)))
            .thenReturn(Optional.empty());

        doPut(AUTHORISATION_TOKEN_CITIZEN, reviewOrder,
            ROOT_PATH + "/{externalId}/review-order", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testSaveReviewOrderAlreadyPresent() throws Exception {
        ReviewOrder reviewOrder = ReviewOrder.builder()
            .reason("blah".repeat(4))
            .requestedAt(LocalDateTime.now())
            .requestedBy(ReviewOrder.RequestedBy.CLAIMANT).build();
        Claim claim = SampleClaim.getDefault().toBuilder()
            .reviewOrder(reviewOrder).build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, CITIZEN))
            .thenReturn(Optional.of(claim));

        doPut(AUTHORISATION_TOKEN_CITIZEN, reviewOrder,
            ROOT_PATH + "/{externalId}/review-order", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isConflict());
    }

}
