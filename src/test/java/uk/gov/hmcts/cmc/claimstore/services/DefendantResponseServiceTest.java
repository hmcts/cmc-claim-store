package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.DefendantResponseRepository;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse.CLAIM_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse.RESPONSE_ID;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseServiceTest {

    private static final ResponseData VALID_APP = SampleResponseData.validDefaults();
    private static final DefendantResponse DEFENDANT_RESPONSE = SampleDefendantResponse.getDefault();
    private static final Claim claim = SampleClaim.getDefault();
    private static final String AUTHORISATION = "Bearer: aaa";

    private DefendantResponseService responseService;

    @Mock
    private DefendantResponseRepository defendantResponseRepository;
    @Mock
    private EventProducer eventProducer;
    @Mock
    private UserService userService;
    @Mock
    private ClaimService claimService;
    @Mock
    private JsonMapper mapper;

    @Before
    public void setup() {
        responseService = new DefendantResponseService(
            defendantResponseRepository, mapper, eventProducer, claimService, userService
        );
    }

    @Test
    public void getResponseByIdShouldCallRepositoryWhenValidDefendantResponseIsReturned() {
        //given
        final DefendantResponse defendantResponse = createDefendantResponse(VALID_APP);
        final Optional<DefendantResponse> result = Optional.of(defendantResponse);

        when(defendantResponseRepository.getById(eq(RESPONSE_ID))).thenReturn(result);
        //when
        final DefendantResponse actual = responseService.getById(RESPONSE_ID);

        //then
        assertThat(actual).isEqualTo(defendantResponse);
    }

    @Test(expected = NotFoundException.class)
    public void getByIdShouldThrowExceptionWhenDefendantResponseDoesNotExist() {
        //given
        final Optional<DefendantResponse> result = Optional.empty();

        when(defendantResponseRepository.getById(eq(RESPONSE_ID))).thenReturn(result);

        //when
        responseService.getById(RESPONSE_ID);
    }

    @Test
    public void getByDefendantIdShouldCallRepositoryWhenValidDefendantResponseIsReturned() {
        //given
        final DefendantResponse defendantResponse = createDefendantResponse(VALID_APP);
        final List<DefendantResponse> result = Collections.singletonList(defendantResponse);

        when(defendantResponseRepository.getByDefendantId(eq(DEFENDANT_ID))).thenReturn(result);

        //when
        final List<DefendantResponse> actual = responseService.getByDefendantId(DEFENDANT_ID);

        //then
        assertThat(actual).isEqualTo(result);
    }

    @Test
    public void saveShouldFinishSuccessfully() {
        //given
        final ResponseData app = SampleResponseData.validDefaults();
        final String jsonApp = new ResourceReader().read("/defendant-response.json");
        when(mapper.toJson(eq(app))).thenReturn(jsonApp);

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(
            SampleUserDetails.builder().withUserId(USER_ID).withMail(DEFENDANT_EMAIL).build());

        when(defendantResponseRepository.save(eq(CLAIM_ID), eq(DEFENDANT_ID), eq(DEFENDANT_EMAIL), eq(jsonApp)))
            .thenReturn(RESPONSE_ID);

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(claim);
        when(defendantResponseRepository.getById(eq(RESPONSE_ID))).thenReturn(Optional.of(DEFENDANT_RESPONSE));

        //when
        final DefendantResponse response = responseService.save(CLAIM_ID, DEFENDANT_ID, app, AUTHORISATION);

        //then
        assertThat(response.getId()).isEqualTo(RESPONSE_ID);
        verify(claimService, once()).getClaimById(eq(CLAIM_ID));
        verify(eventProducer, once())
            .createDefendantResponseEvent(eq(claim), eq(DEFENDANT_RESPONSE));
    }

    private static DefendantResponse createDefendantResponse(final ResponseData responseData) {
        return new DefendantResponse(RESPONSE_ID, CLAIM_ID, DEFENDANT_ID, DEFENDANT_EMAIL,
            responseData, NOW_IN_LOCAL_ZONE);
    }

    @Test
    public void shouldReturnResponseIfFoundByClaimId() {
        DefendantResponse defendantResponse = createDefendantResponse(VALID_APP);
        when(defendantResponseRepository.getByClaimId(anyLong())).thenReturn(Optional.of(defendantResponse));

        assertThat(responseService.getByClaimId(123L)).isEqualTo(defendantResponse);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowInvalidApplicationWhenNotFoundByClaimId() {
        when(defendantResponseRepository.getByClaimId(anyLong())).thenReturn(Optional.empty());
        responseService.getByClaimId(123L);
    }
}
