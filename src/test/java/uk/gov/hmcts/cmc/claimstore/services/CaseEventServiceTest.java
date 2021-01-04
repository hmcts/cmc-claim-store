package uk.gov.hmcts.cmc.claimstore.services;

import groovy.lang.IntRange;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.ScheduledStateTransitionContentProvider;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.services.statetransition.StateTransition;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseEventServiceTest {

    private CaseEventService caseEventService;

    @Mock
    private CaseEventsApi caseEventsApi;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private final User user = new User("", new UserDetails(null, null, null, null, null));

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String SERVICE_AUTHORISATION = "122FSDFSFDSFDSFdsafdsfadsfasdfaaa2323232";
    private static final String UUID = "UUID";

    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";


    private List<CaseEvent> caseEventList = new ArrayList<>();
    private List<CaseEventDetail> caseEventDetailList = new ArrayList<>();

    @Before
    public void setUp() {

        caseEventService = new CaseEventService(
            caseEventsApi,
            userService,
            authTokenGenerator
        );

        caseEventList.add(CaseEvent.CLOSE_AWAITING_RESPONSE_HWF);

        caseEventDetailList.add(CaseEventDetail.builder().id(CaseEvent.CLOSE_AWAITING_RESPONSE_HWF.getValue())
            .eventName(CaseEvent.CLOSE_AWAITING_RESPONSE_HWF.name()).createdDate(LocalDateTime.now()).build());
    }

    @Test
    public void shouldFindEventsForCases() {
        when(userService.getUser(AUTHORISATION)).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORISATION);
        //when(user.getUserDetails()).thenReturn(userDetails);
        //when(userDetails.getId()).thenReturn(UUID);
        when(caseEventsApi.findEventDetailsForCase(user.getAuthorisation(), SERVICE_AUTHORISATION,
            user.getUserDetails().getId(),
            JURISDICTION_ID, CASE_TYPE_ID, "1")).
            thenReturn(caseEventDetailList);
        List<CaseEvent> caseEventListOutput = caseEventService.findEventsForCase(AUTHORISATION, "1");
        assertThat(caseEventListOutput).isEqualTo(caseEventList);
    }
}
