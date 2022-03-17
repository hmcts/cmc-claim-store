package uk.gov.hmcts.cmc.claimstore.services.pilotcourt;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourtMapper;
import uk.gov.hmcts.cmc.claimstore.test.utils.DataFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PilotCourtServiceTest {

    @Mock
    private AppInsights appInsights;

    @Mock
    private CourtFinderApi courtFinderApi;

    @Mock
    private HearingCourtMapper hearingCourtMapper;

    @Mock
    private feign.Request request;

    private final static String COURT_FINDER_RESPONSE_NEWCASTLE = "court-finder/response/NEWCASTLE_COURT_FINDER_RESPONSE.json";
    private final static String COURT_DETAILS_NEWCASTLE = "court-details/NEWCASTLE_COURT_DETAILS.json";
    private final static String HEARING_COURT_NEWCASTLE = "hearing-court/NEWCASTLE_HEARING_COURT.json";
    private final static String PILOT_COURT_ALL_COURTS = "pilot-court/response/ALL_PILOT_COURT_IDS.json";
    private final static String PILOT_COURT_IDS = "pilot-court/response/PILOT_COURT_IDS.json";
    private final static String CSV_PATH = "/pilot-court/pilot-courts.csv";
    private final static String CSV_PATH_SINGLE = "/pilot-court/pilot-courts-single.csv";
    private final static String CSV_PATH_INVALID = "/pilot-court/pilot-courts-invalid.csv";
    private final static String CSV_PATH_LA = "/pilot-court/pilot-courts-LA.csv";
    private final static String CSV_PATH_JDDO = "/pilot-court/pilot-courts-JDDO.csv";
    private final static String CSV_PATH_NAMES = "/pilot-court/pilot-courts-names.csv";
    private final static String CSV_PATH_DATES = "/pilot-court/pilot-courts-dates.csv";
    private final static String CSV_PATH_COURT_IDS = "/pilot-court/pilot-courts-court-ids.csv";

    @Nested
    @DisplayName("Init")
    class InitTests {

        @Test
        void shouldThrowIllegalStateExceptionOnInvalidCSVPath() {
            PilotCourtService pilotCourtService = new PilotCourtService(
                "InvalidPath",
                courtFinderApi,
                appInsights
            );

            Assertions.assertThrows(IllegalStateException.class, pilotCourtService::init);
        }

        @Test
        void shouldBuildListOfCourtsFromCSV() {
            PilotCourtService pilotCourtService = new PilotCourtService(
                CSV_PATH,
                courtFinderApi,
                appInsights
            );

            pilotCourtService.init();

            Set<String> actualPilotCourtIds = pilotCourtService.getAllPilotCourtIds();
            Set<String> expectedPilotCourtIds = DataFactory.createStringSetFromJson(PILOT_COURT_ALL_COURTS);

            assertEquals(expectedPilotCourtIds, actualPilotCourtIds);
        }
    }

    @Test
    void shouldThrowRIllegalArgumentExceptionOnUnknownCourtId() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            CSV_PATH_SINGLE,
            courtFinderApi,
            appInsights
        );

        pilotCourtService.init();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            pilotCourtService.getPilotHearingCourt("UNKNOWN_ID"));
    }

    @Test
    void shouldReturnHearingCourt() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            CSV_PATH_COURT_IDS,
            courtFinderApi,
            appInsights
        );

        CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
            .thenReturn(courtFinderResponse);

        pilotCourtService.init();

        CourtDetails courtDetails = DataFactory.createCourtDetailsFromJson(COURT_DETAILS_NEWCASTLE);

        when(courtFinderApi.getCourtDetailsFromNameSlug(anyString()))
            .thenReturn(courtDetails);

        HearingCourt actualHearingCourt = new HearingCourt();
        Optional<HearingCourt> hearingCourt = pilotCourtService.getPilotHearingCourt("NEWCASTLE");
        if (hearingCourt.isPresent()) {
            actualHearingCourt = hearingCourt.get();
        }

        HearingCourt expectedHearingCourt = DataFactory.createHearingCourtFromJson(HEARING_COURT_NEWCASTLE);

        assertEquals(expectedHearingCourt, actualHearingCourt);
    }

//    todo : make actual test
    @Test
    void shouldFetchHearingCourtOnDemandIfNotAlreadyExist() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            CSV_PATH_SINGLE,
            courtFinderApi,
            appInsights
        );

        //Simulate court finder being down on init
        CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

        Request request = Request.create(Request.HttpMethod.GET, "URL", ImmutableMap.of(), Request.Body.empty(), null);
        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString())).thenThrow(FeignException.errorStatus("",
            Response.builder().request(request).build()))
            .thenReturn(courtFinderResponse);

        pilotCourtService.init();

        HearingCourt expectedHearingCourt = DataFactory.createHearingCourtFromJson(HEARING_COURT_NEWCASTLE);

        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
            .thenReturn(courtFinderResponse);

        CourtDetails courtDetails = DataFactory.createCourtDetailsFromJson(COURT_DETAILS_NEWCASTLE);

        when(courtFinderApi.getCourtDetailsFromNameSlug(anyString()))
            .thenReturn(courtDetails);

        HearingCourt actualHearingCourt = new HearingCourt();
        Optional<HearingCourt> birminghamHearingCourt = pilotCourtService.getPilotHearingCourt("BIRMINGHAM");
        if (birminghamHearingCourt.isPresent()) {
            actualHearingCourt = birminghamHearingCourt.get();
        }

        assertEquals(expectedHearingCourt, actualHearingCourt);
    }

    @Test
    void shouldReturnAnEmptyOptionalIfCourtFinderReturnsNothing() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            CSV_PATH_SINGLE,
            courtFinderApi,
            appInsights
        );

        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
            .thenReturn(null);

        pilotCourtService.init();

        Optional<HearingCourt> actualHearingCourt = pilotCourtService.getPilotHearingCourt("BIRMINGHAM");

        assertEquals(Optional.empty(), actualHearingCourt);
    }

    @Test
    void shouldThrowValidationErrorForMissingValuesFromCSV() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            CSV_PATH_INVALID,
            courtFinderApi,
            appInsights
        );

        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
            .thenReturn(null);

        Assertions.assertThrows(AssertionError.class, pilotCourtService::init);
    }

    @Test
    void shouldReturnListOfCourtIdsForPilot() {
        CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
            .thenReturn(courtFinderResponse);

        CourtDetails courtDetails = DataFactory.createCourtDetailsFromJson(COURT_DETAILS_NEWCASTLE);

        when(courtFinderApi.getCourtDetailsFromNameSlug(anyString()))
            .thenReturn(courtDetails);

        PilotCourtService pilotCourtService = new PilotCourtService(
            CSV_PATH_COURT_IDS,
            courtFinderApi,
            appInsights
        );

        pilotCourtService.init();

        LocalDateTime claimCreatedDate = LocalDateTime.of(2019, 9, 9, 11, 0, 0);

        Set<String> actualPilotHearingCourtNames = pilotCourtService.getPilotHearingCourts(Pilot.LA, claimCreatedDate)
                .stream()
                .map(HearingCourt::getName)
                .collect(Collectors.toSet());

        Set<String> expectedPilotHearingCourtNames = Collections.singleton("Newcastle Civil & Family Courts and Tribunals Centre");

        assertEquals(expectedPilotHearingCourtNames, actualPilotHearingCourtNames);
    }

    @Nested
    @DisplayName("Is Pilot Court tests")
    class IsPilotCourtTests {

        private PilotCourtService pilotCourtService;

        private final String pilotCourtName = "Edmonton County Court and Family Court";

        private final String nonPilotCourtName = "Manchester Civil Justice Centre (Civil and Family Courts)";

        @Nested
        @DisplayName("Pilot tests")
        class PilotTests {

            @Nested
            @DisplayName("LA Pilot tests")
            class LAPilotTests {

                @BeforeEach
                void setUp() {
                    CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

                    when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
                        .thenReturn(courtFinderResponse);

                    pilotCourtService = new PilotCourtService(
                        CSV_PATH_LA,
                        courtFinderApi,
                        appInsights
                    );

                    pilotCourtService.init();
                }

                @Test
                void shouldReturnTrueIfCourtIsPilotCourt() {
                    assertTrue(pilotCourtService.isPilotCourt(pilotCourtName, Pilot.LA, LocalDateTime.MAX));
                }

                @Test
                void shouldReturnFalseIfCourtIsNotPilotCourt() {
                    assertFalse(pilotCourtService.isPilotCourt(nonPilotCourtName, Pilot.LA, LocalDateTime.MAX));
                }
            }

            @Nested
            @DisplayName("JDDO Pilot tests")
            class JDDOPilotTests {

                @BeforeEach
                void setUp() {
                    pilotCourtService = new PilotCourtService(
                        CSV_PATH_JDDO,
                        courtFinderApi,
                        appInsights
                    );

                    pilotCourtService.init();
                }

                @Test
                void shouldReturnTrueIfCourtIsPilotCourt() {
                    CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

                    when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
                        .thenReturn(courtFinderResponse);

                    assertTrue(pilotCourtService.isPilotCourt(pilotCourtName, Pilot.JDDO, LocalDateTime.MAX));
                }

                @Test
                void shouldReturnFalseIfCourtIsNotPilotCourt() {
                    assertFalse(pilotCourtService.isPilotCourt(nonPilotCourtName, Pilot.JDDO, LocalDateTime.MAX));
                }
            }
        }

        @Nested
        @DisplayName("Court name tests")
        class CourtNameTests {

            @BeforeEach
            void setUp() {
                pilotCourtService = new PilotCourtService(
                    CSV_PATH_NAMES,
                    courtFinderApi,
                    appInsights
                );

                pilotCourtService.init();
            }

            @Nested
            @DisplayName("Full name tests")
            class FullNameTests {

                @Test
                void shouldReturnTrueIfNameIsMatched() {
                    assertTrue(pilotCourtService.isPilotCourt(pilotCourtName, Pilot.values()[0], LocalDateTime.MAX));
                }

                @Test
                void shouldReturnFalseIfNameIsNotMatched() {
                    assertFalse(pilotCourtService
                        .isPilotCourt(nonPilotCourtName, Pilot.values()[0], LocalDateTime.MAX));
                }
            }

            @Nested
            @DisplayName("Partial name tests")
            class PartialNameTests {

                @Test
                void shouldReturnTrueIfNameIsMatched() {
                    String partialPilotCourtName = "Edmonton";
                    assertTrue(pilotCourtService
                        .isPilotCourt(partialPilotCourtName, Pilot.values()[0], LocalDateTime.MAX));
                }

                @Test
                void shouldReturnFalseIfNameIsNotMatched() {
                    CourtFinderResponse courtFinderResponse = CourtFinderResponse.builder().build();

                    when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                        .thenReturn(courtFinderResponse);

                    String partialNonPilotCourtName = "Manchester";
                    assertFalse(pilotCourtService
                        .isPilotCourt(partialNonPilotCourtName, Pilot.values()[0], LocalDateTime.MAX));
                }

            }

        }

        @Nested
        @DisplayName("Created date tests")
        class CreatedDateTests {
            private final LocalDateTime goLiveDate = LocalDateTime.of(2019, 9, 9, 11, 0, 0);
            private final Pilot pilot = Pilot.LA;

            @BeforeEach
            void setUp() {

                pilotCourtService = new PilotCourtService(
                    CSV_PATH_DATES,
                    courtFinderApi,
                    appInsights
                );

                CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

                when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
                    .thenReturn(courtFinderResponse);

                pilotCourtService.init();
            }

            @Test
            void shouldReturnTrueIfCreatedDateIsOnGoLiveDate() {
                assertTrue(pilotCourtService
                    .isPilotCourt(pilotCourtName, pilot, goLiveDate));
            }

            @Test
            void shouldReturnTrueIfCreatedDateIsAfterGoLiveDate() {
                assertTrue(pilotCourtService
                    .isPilotCourt(pilotCourtName, pilot, goLiveDate.plus(1, ChronoUnit.SECONDS)));
            }

            @Test
            void shouldReturnFalseIfCreatedDateIsBeforeGoLiveDate() {
                assertFalse(pilotCourtService
                    .isPilotCourt(pilotCourtName, pilot, goLiveDate.minus(1, ChronoUnit.SECONDS)));
            }

        }

    }

}
