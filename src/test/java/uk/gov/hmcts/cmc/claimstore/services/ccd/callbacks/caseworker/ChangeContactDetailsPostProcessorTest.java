package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactChangeContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.ChangeContactDetailsPostProcessor.NO_DETAILS_CHANGED_ERROR;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;

@ExtendWith(MockitoExtension.class)
public class ChangeContactDetailsPostProcessorTest {
    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_NAME = "documentName";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private UserService userService;
    @Mock
    private ChangeContactDetailsNotificationService changeContactDetailsNotificationService;
    @Mock
    private CaseDetails caseDetailsBefore;
    @Mock
    private CaseDetails caseDetails;
    @Mock
    private ChangeContactLetterService changeContactLetterService;
    private ChangeContactDetailsPostProcessor changeContactDetailsPostProcessor;
    private CallbackRequest callbackRequest;
    private CallbackParams callbackParams;
    @Mock
    private CCDCaseApi ccdCaseApi;

    private static final String AUTHORISATION = "Bearer: aaaa";

    @BeforeEach
    void setUp() {
        changeContactDetailsPostProcessor = new ChangeContactDetailsPostProcessor(
            caseDetailsConverter,
            changeContactLetterService,
            changeContactDetailsNotificationService,
            new LetterContentBuilder(),
            userService,
            ccdCaseApi
        );
    }

    @Test
    public void shouldReturnNewContactDetails() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        Claim claim = Claim.builder().build();

        CCDCase updatedCCDCase = ccdCase.toBuilder()
            .contactChangeParty(CCDContactPartyType.CLAIMANT)
            .applicants(ImmutableList.of(CCDCollectionElement.<CCDApplicant>builder()
                .value(ccdCase.getApplicants().get(0).getValue().toBuilder()
                    .partyDetail(SampleData.getCCDPartyWithEmail("some@mail.com"))
                    .build())
                .build()))
            .build();

        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(updatedCCDCase);
        when(caseDetailsConverter.convertTo(claim)).thenReturn(ccdCase);
        when(ccdCaseApi.getByExternalId(anyString(), anyString())).thenReturn(Optional.of(claim));

        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(caseDetails)
            .build();

        callbackParams = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse
            = (AboutToStartOrSubmitCallbackResponse) changeContactDetailsPostProcessor
            .showNewContactDetails(callbackParams);

        Map<String, Object> data = callbackResponse.getData();
        assertThat(data).isNotEmpty()
            .contains(Maps.immutableEntry("contactChangeContent", CCDContactChangeContent.builder()
                .telephone("0776655443322")
                .isTelephoneModified(CCDYesNoOption.YES)
                .primaryEmail("some@mail.com")
                .isEmailModified(CCDYesNoOption.YES)
                .isPrimaryAddressModified(CCDYesNoOption.NO)
                .isCorrespondenceAddressModified(CCDYesNoOption.NO)
                .telephoneRemoved(CCDYesNoOption.NO)
                .primaryEmailRemoved(CCDYesNoOption.NO)
                .correspondenceAddressRemoved(CCDYesNoOption.NO)
                .build()));
    }

    @Test
    public void shouldPrintAndUpdateCaseDocumentsIfDefendantNotLinked() {
        CCDDocument draftLetterDoc = CCDDocument.builder().documentUrl(DOC_URL).documentFileName(DOC_NAME).build();
        CCDCase ccdCase = SampleData.getCCDCitizenCaseWithRespondent(CCDRespondent.builder().defendantId(null)
            .build()).toBuilder()
            .contactChangeParty(CCDContactPartyType.CLAIMANT)
            .contactChangeContent(CCDContactChangeContent.builder()
                .telephone("0776655443322")
                .isTelephoneModified(CCDYesNoOption.YES)
                .primaryEmail("some@mail.com")
                .isEmailModified(CCDYesNoOption.YES)
                .isPrimaryAddressModified(CCDYesNoOption.NO)
                .isCorrespondenceAddressModified(CCDYesNoOption.NO)
                .telephoneRemoved(CCDYesNoOption.NO)
                .primaryEmailRemoved(CCDYesNoOption.NO)
                .correspondenceAddressRemoved(CCDYesNoOption.NO)
                .build())
            .draftLetterDoc(draftLetterDoc)
            .build();

        Claim claim = SampleClaim.getCitizenClaim().toBuilder().build();

        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(caseDetails)).thenReturn(claim);
        given(userService.getUserDetails(anyString())).willReturn(SampleUserDetails.getDefault());

        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        callbackParams = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, AUTHORISATION_TOKEN))
            .build();

        changeContactDetailsPostProcessor.performPostProcesses(callbackParams);

        verify(changeContactLetterService).publishLetter(any(CCDCase.class), any(Claim.class), any(String.class),
            any(CCDDocument.class));
    }

    @Test
    public void shouldSendEmailToRightRecipientWhenCaseIsLinkedAndChangeMadeForClaimant() {
        CCDCase ccdCase = SampleData.getCCDCitizenCaseWithRespondent(CCDRespondent.builder().defendantId(DEFENDANT_ID)
            .build()).toBuilder()
            .contactChangeParty(CCDContactPartyType.CLAIMANT)
            .build();

        Claim claim = SampleClaim.builder().build();

        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(caseDetails)).thenReturn(claim);

        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(caseDetails)
            .build();

        callbackParams = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, AUTHORISATION_TOKEN))
            .build();

        changeContactDetailsPostProcessor.performPostProcesses(callbackParams);
        verify(changeContactDetailsNotificationService).sendEmailToRightRecipient(eq(ccdCase), eq(claim));
    }

    @Test
    public void shouldSendEmailToRightRecipientWhenMadeForDefendant() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList()).toBuilder()
            .contactChangeParty(CCDContactPartyType.DEFENDANT)
            .build();

        Claim claim = SampleClaim.builder().build();

        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(caseDetails)).thenReturn(claim);

        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(caseDetails)
            .build();

        callbackParams = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, AUTHORISATION_TOKEN))
            .build();

        changeContactDetailsPostProcessor.performPostProcesses(callbackParams);
        verify(changeContactDetailsNotificationService).sendEmailToRightRecipient(eq(ccdCase), eq(claim));
    }

    @Test
    public void shouldReturnErrorIfNoContactDetailsWereChanged() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());

        Claim claim = Claim.builder().build();
        when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
        when(caseDetailsConverter.convertTo(claim)).thenReturn(ccdCase);
        when(ccdCaseApi.getByExternalId(anyString(), anyString())).thenReturn(Optional.of(claim));
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(caseDetails)
            .build();

        callbackParams = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, AUTHORISATION_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse
            = (AboutToStartOrSubmitCallbackResponse) changeContactDetailsPostProcessor
            .showNewContactDetails(callbackParams);

        assertThat(callbackResponse.getErrors())
            .contains(NO_DETAILS_CHANGED_ERROR);
    }

}
