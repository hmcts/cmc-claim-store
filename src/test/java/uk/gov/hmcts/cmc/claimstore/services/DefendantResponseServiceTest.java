package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.DefendantResponseRepository;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseServiceTest {

    private static final ResponseData VALID_APP = SampleResponseData.validDefaults();
    private static final Claim claim = SampleClaim.getDefault();
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String DEFENDANT_EMAIL = "test@example.com";

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
    public void saveShouldFinishSuccessfully() {
        //given
        final ResponseData app = SampleResponseData.validDefaults();
        final String jsonApp = new ResourceReader().read("/defendant-response.json");
        when(mapper.toJson(eq(app))).thenReturn(jsonApp);

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(
            new UserDetails(USER_ID, DEFENDANT_EMAIL, "Jonny", "Jones")
        );
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(
            SampleUserDetails.builder().withUserId(USER_ID).withMail(DEFENDANT_EMAIL).build());

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(claim);

        //when
        responseService.save(CLAIM_ID, DEFENDANT_ID, app, AUTHORISATION);

        //then
        verify(claimService, once()).getClaimById(eq(CLAIM_ID));
        verify(eventProducer, once())
            .createDefendantResponseEvent(eq(claim) );
    }

}
