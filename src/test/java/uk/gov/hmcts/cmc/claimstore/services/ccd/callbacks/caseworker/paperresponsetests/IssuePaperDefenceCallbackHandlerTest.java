package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperresponsetests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.launchdarkly.sdk.LDUser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgmentType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDeadlineService;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.DocumentPublishService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.IssuePaperDefenceCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.IssuePaperResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@ExtendWith(MockitoExtension.class)
class IssuePaperDefenceCallbackHandlerTest {
    private static final String ERROR_MESSAGE = "There was a technical problem. Nothing has been sent."
        + " You need to try again.";
    private static final String CLAIMANT_ISSUED_CCJ =
        "OCON9x form cannot be sent out as CCJ already issued by claimant.";
    private static final String AUTHORISATION = "auth";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private IssueDateCalculator issueDateCalculator;
    @Mock
    private DocumentPublishService documentPublishService;
    @Mock
    private IssuePaperResponseNotificationService issuePaperResponseNotificationService;
    @Mock
    private LaunchDarklyClient launchDarklyClient;
    @Mock
    private ClaimDeadlineService claimDeadlineService;

    private Claim claim;
    private CallbackRequest callbackRequest;
    private IssuePaperDefenceCallbackHandler issuePaperDefenceCallbackHandler;
    private CCDCase ccdCase;

    @BeforeEach
    void setUp() {
        issuePaperDefenceCallbackHandler = getIssuePaperDefenceCallbackHandler();
        claim = getClaim();
    }

    private Claim getClaim() {
        return Claim.builder()
            .claimData(SampleClaimData.builder().build())
            .defendantEmail("email@email.com")
            .defendantId("id")
            .submitterEmail("email@email.com")
            .referenceNumber("ref. number")
            .issuedOn(LocalDate.now())
            .build();
    }

    @NotNull
    private IssuePaperDefenceCallbackHandler getIssuePaperDefenceCallbackHandler() {
        return new IssuePaperDefenceCallbackHandler(
            caseDetailsConverter,
            responseDeadlineCalculator,
            issueDateCalculator,
            issuePaperResponseNotificationService,
            documentPublishService,
            launchDarklyClient,
            claimDeadlineService
        );
    }

    @Test
    void shouldHandleAboutToSubmitCallback() {
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("000MC001")
            .respondents(ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                    .build()
            ))
            .applicants(List.of(
                CCDCollectionElement.<CCDApplicant>builder()
                    .value(SampleData.getCCDApplicantIndividual())
                    .build()
            ))
            .build();

        LocalDate date = LocalDate.now();
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculateResponseDeadline(any(LocalDate.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(any(LocalDate.class)))
            .thenReturn(date);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        callbackRequest =
            getCallbackRequest(caseDetails);

        CallbackParams callbackParams = getBuild();

        issuePaperDefenceCallbackHandler.handle(callbackParams);

        verify(documentPublishService).publishDocuments(any(CCDCase.class), any(Claim.class), eq(AUTHORISATION),
            eq(date));
        verify(issuePaperResponseNotificationService).notifyClaimant(any(Claim.class));
    }

    @Test
    void shouldHandleAboutToSubmitCallbackWhenOconResend() {
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("000MC001")
            .respondents(ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                    .build()
            ))
            .applicants(List.of(
                CCDCollectionElement.<CCDApplicant>builder()
                    .value(SampleData.getCCDApplicantIndividual())
                    .build()
            ))
            .build();

        CCDCase updatedCCDCase = ccdCase.toBuilder()
            .respondents(ImmutableList.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(ccdCase.getRespondents().get(0).getValue().toBuilder()
                    .paperFormServedDate(LocalDate.now())
                    .countyCourtJudgmentRequest(null)
                    .build())
                .build()))
            .extendedResponseDeadline(LocalDate.now())
            .build();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(updatedCCDCase);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        callbackRequest =
            getCallbackRequest(caseDetails);

        CallbackParams callbackParams = getBuild();

        issuePaperDefenceCallbackHandler.handle(callbackParams);
        verify(documentPublishService).publishDocuments(any(CCDCase.class), any(Claim.class), eq(AUTHORISATION),
            eq(LocalDate.now()));
        verify(issuePaperResponseNotificationService, times(0)).notifyClaimant(any(Claim.class));
    }

    @Test
    void shouldHandleAboutToSubmitCallbackMoreTimeRequested() {
        CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
            .type(CCDCountyCourtJudgmentType.DETERMINATION)
            .build();
        ccdCase = getCCDData(ccdCountyCourtJudgment, CCDYesNoOption.YES);

        CCDCase updatedCCDCase = ccdCase.toBuilder()
            .extendedResponseDeadline(LocalDate.now())
            .build();

        LocalDate date = LocalDate.now();
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(any(LocalDate.class)))
            .thenReturn(date);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(updatedCCDCase);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        callbackRequest =
            getCallbackRequest(caseDetails);

        CallbackParams callbackParams = getBuild();

        issuePaperDefenceCallbackHandler.handle(callbackParams);
        verify(documentPublishService).publishDocuments(any(CCDCase.class), any(Claim.class), eq(AUTHORISATION),
            eq(LocalDate.now()));
        verify(issuePaperResponseNotificationService).notifyClaimant(any(Claim.class));
    }

    @Test
    void shouldSendErrorsWhenExceptionThrownForCallback() {
        CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
            .type(CCDCountyCourtJudgmentType.ADMISSIONS)
            .build();
        ccdCase = getCCDData(ccdCountyCourtJudgment, CCDYesNoOption.YES);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        callbackRequest =
            getCallbackRequest(caseDetails);
        CallbackParams callbackParams = getBuild();
        LocalDate date = LocalDate.now();
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculateResponseDeadline(any(LocalDate.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(any(LocalDate.class)))
            .thenReturn(date);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(documentPublishService.publishDocuments(ccdCase, claim, AUTHORISATION, LocalDate.now()))
            .thenThrow(DocumentGenerationFailedException.class);
        AboutToStartOrSubmitCallbackResponse actualResponse =
            (AboutToStartOrSubmitCallbackResponse) issuePaperDefenceCallbackHandler.handle(callbackParams);
        assertThat(actualResponse.getErrors().get(0)).isEqualTo(ERROR_MESSAGE);
    }

    @Test
    void whenClaimantIssuedCCJ() {
        CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
            .type(CCDCountyCourtJudgmentType.DEFAULT)
            .build();
        ccdCase = getCCDData(ccdCountyCourtJudgment, CCDYesNoOption.YES);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        callbackRequest =
            getCallbackRequest(caseDetails);
        CallbackParams callbackParams = getBuild();
        LocalDate date = LocalDate.now();
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(any(LocalDate.class)))
            .thenReturn(date);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(launchDarklyClient.isFeatureEnabled(eq("ocon-enhancements"), any(LDUser.class))).thenReturn(false);
        when(launchDarklyClient.isFeatureEnabled(eq("ocon-enhancement-2"), any(LDUser.class))).thenReturn(false);
        AboutToStartOrSubmitCallbackResponse actualResponse =
            (AboutToStartOrSubmitCallbackResponse) issuePaperDefenceCallbackHandler.handle(callbackParams);
        assertThat(actualResponse.getErrors().get(0)).isEqualTo(CLAIMANT_ISSUED_CCJ);
    }

    private CallbackRequest getCallbackRequest(CaseDetails caseDetails) {
        return CallbackRequest.builder()
            .eventId(CaseEvent.ISSUE_PAPER_DEFENSE_FORMS.getValue())
            .caseDetails(caseDetails)
            .build();
    }

    private CCDCase getCCDData(CCDCountyCourtJudgment ccdCountyCourtJudgment, CCDYesNoOption option) {
        return CCDCase.builder()
            .issuedOn(LocalDate.now().minusDays(20))
            .previousServiceCaseReference("000MC001")
            .respondents(ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQInClaimantResponse().toBuilder()
                        .countyCourtJudgmentRequest(ccdCountyCourtJudgment)
                        .responseMoreTimeNeededOption(option)
                        .build())
                    .build()
            ))
            .applicants(List.of(
                CCDCollectionElement.<CCDApplicant>builder()
                    .value(SampleData.getCCDApplicantIndividual())
                    .build()
            ))
            .build();
    }

    @Test
    void whenClaimantIssuedCCJButNoValidation() {
        CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
            .type(CCDCountyCourtJudgmentType.DEFAULT)
            .build();
        ccdCase = getCCDData(ccdCountyCourtJudgment, CCDYesNoOption.YES);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        callbackRequest = getCallbackRequest(caseDetails);
        CallbackParams callbackParams = getBuild();
        LocalDate date = LocalDate.now();
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(any(LocalDate.class)))
            .thenReturn(date);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(launchDarklyClient.isFeatureEnabled(eq("ocon-enhancements"), any(LDUser.class))).thenReturn(true);
        when(launchDarklyClient.isFeatureEnabled(eq("ocon-enhancement-2"), any(LDUser.class))).thenReturn(false);
        AboutToStartOrSubmitCallbackResponse actualResponse =
            (AboutToStartOrSubmitCallbackResponse) issuePaperDefenceCallbackHandler.handle(callbackParams);
        Assertions.assertNull(actualResponse.getErrors());
    }

    private CallbackParams getBuild() {
        return CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    @Test
    void shouldHaveCorrectCaseworkerRole() {
        assertThat(issuePaperDefenceCallbackHandler.getSupportedRoles()).containsOnly(CASEWORKER);
    }

    @Nested
    @DisplayName("RequestDeadlineScenario")
    class RequestDeadlineTest {
        @BeforeEach
        void setUp() {

            issuePaperDefenceCallbackHandler = getIssuePaperDefenceCallbackHandler();
            claim = getClaim();
            LocalDate date = LocalDate.now();
            when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(date);
            when(responseDeadlineCalculator.calculateResponseDeadline(any(LocalDate.class))).thenReturn(date);
            when(responseDeadlineCalculator.calculatePostponedResponseDeadline(any(LocalDate.class)))
                .thenReturn(date);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(launchDarklyClient.isFeatureEnabled(eq("ocon-enhancements"), any(LDUser.class))).thenReturn(false);
            when(launchDarklyClient.isFeatureEnabled(eq("ocon-enhancement-2"), any(LDUser.class))).thenReturn(true);
        }

        @Test
        void whenResponseDeadlinePassedSendingOCON() {
            CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
                .type(CCDCountyCourtJudgmentType.DEFAULT)
                .build();
            ccdCase = getCCDData(ccdCountyCourtJudgment, CCDYesNoOption.YES);
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .data(Collections.emptyMap())
                .build();
            callbackRequest =
                getCallbackRequest(caseDetails);
            CallbackParams callbackParams = getBuild();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(claimDeadlineService.isPastDeadline(any(LocalDateTime.class), any(LocalDate.class))).thenReturn(true);
            AboutToStartOrSubmitCallbackResponse actualResponse =
                (AboutToStartOrSubmitCallbackResponse) issuePaperDefenceCallbackHandler.handle(callbackParams);
            assertThat(actualResponse.getErrors().get(0)).isEqualTo(CLAIMANT_ISSUED_CCJ);
        }

        @Test
        void whenResponseDeadlineNotPassedMoretimeTimeRequesedSendingOCON() {
            CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
                .type(CCDCountyCourtJudgmentType.DEFAULT)
                .build();
            ccdCase = getCCDData(ccdCountyCourtJudgment, CCDYesNoOption.YES);
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .data(Collections.emptyMap())
                .build();
            callbackRequest =
                getCallbackRequest(caseDetails);
            CallbackParams callbackParams = getBuild();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(claimDeadlineService.isPastDeadline(any(LocalDateTime.class), any(LocalDate.class))).thenReturn(false);
            AboutToStartOrSubmitCallbackResponse actualResponse =
                (AboutToStartOrSubmitCallbackResponse) issuePaperDefenceCallbackHandler.handle(callbackParams);
            assertThat(actualResponse.getErrors().get(0)).isEqualTo(CLAIMANT_ISSUED_CCJ);
        }

        @Test
        void whenResponseDeadlinePassedMoretimeTimeNotRequesedSendingOCON() {
            CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
                .type(CCDCountyCourtJudgmentType.DEFAULT)
                .build();
            ccdCase = getCCDData(ccdCountyCourtJudgment, CCDYesNoOption.NO);

            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .data(Collections.emptyMap())
                .build();
            callbackRequest =
                getCallbackRequest(caseDetails);
            CallbackParams callbackParams = getBuild();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(claimDeadlineService.isPastDeadline(any(LocalDateTime.class), any(LocalDate.class))).thenReturn(true);
            AboutToStartOrSubmitCallbackResponse actualResponse =
                (AboutToStartOrSubmitCallbackResponse) issuePaperDefenceCallbackHandler.handle(callbackParams);
            assertThat(actualResponse.getErrors().get(0)).isEqualTo(CLAIMANT_ISSUED_CCJ);
        }

        @Test
        void whenResponseDeadlineNotPassedMoretimeTimeNotRequesedSendingOCON() {
            CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
                .type(CCDCountyCourtJudgmentType.DEFAULT)
                .build();
            ccdCase = getCCDData(ccdCountyCourtJudgment, CCDYesNoOption.NO);

            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .data(Collections.emptyMap())
                .build();
            callbackRequest =
                getCallbackRequest(caseDetails);
            CallbackParams callbackParams = getBuild();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(claimDeadlineService.isPastDeadline(any(LocalDateTime.class), any(LocalDate.class))).thenReturn(false);
            AboutToStartOrSubmitCallbackResponse actualResponse =
                (AboutToStartOrSubmitCallbackResponse) issuePaperDefenceCallbackHandler.handle(callbackParams);
            assertThat(actualResponse.getErrors().get(0)).isEqualTo(CLAIMANT_ISSUED_CCJ);
        }
    }

}

