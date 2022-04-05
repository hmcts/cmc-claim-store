package uk.gov.hmcts.cmc.claimstore.services.pilotcourt;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.Court;
import uk.gov.hmcts.cmc.claimstore.requests.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourtMapper;
import uk.gov.hmcts.cmc.claimstore.services.courtfinder.CourtFinderService;
import uk.gov.hmcts.cmc.claimstore.test.utils.DataFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PilotCourtServiceTest {

    @Mock
    private AppInsights appInsights;

    @Mock
    private CourtFinderApi courtFinderApi;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private HearingCourtMapper hearingCourtMapper;

    @Mock
    private feign.Request request;

    private final String csvPath = "/pilot-court/pilot-courts.csv";
    private final String csvPathSingle = "/pilot-court/pilot-courts-single.csv";
    private final String csvPathInvalid = "/pilot-court/pilot-courts-invalid.csv";
    private final String csvPathLa = "/pilot-court/pilot-courts-LA.csv";
    private final String csvPathJddo = "/pilot-court/pilot-courts-JDDO.csv";
    private final String csvPathNames = "/pilot-court/pilot-courts-names.csv";
    private final String csvPathDates = "/pilot-court/pilot-courts-dates.csv";
    private final String csvPathCourtIds = "/pilot-court/pilot-courts-court-ids.csv";
    private static final String HEARING_COURT_NEWCASTLE = "hearing-court/NEWCASTLE_HEARING_COURT.json";
    private static final String LIST_COURTS_SINGLE_NEWCASTLE = "courtfinder/COURT_LIST_SINGLE_NEWCASTLE.json";
    private static final String PILOT_COURT_ALL_COURTS = "pilot-court/response/ALL_PILOT_COURT_IDS.json";

    @Test
    void shouldThrowRIllegalArgumentExceptionOnUnknownCourtId() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathSingle,
            courtFinderService,
            hearingCourtMapper,
            appInsights
        );
        pilotCourtService.init();
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            pilotCourtService.getPilotHearingCourt("UNKNOWN_ID"));
    }

    @Test
    void shouldReturnHearingCourt() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathSingle,
            courtFinderService,
            hearingCourtMapper,
            appInsights
        );

        List<Court> courtDetailsListFromPostcode = DataFactory.createCourtListFromJson(LIST_COURTS_SINGLE_NEWCASTLE);

        when(courtFinderService.getCourtDetailsListFromPostcode(anyString()))
            .thenReturn(courtDetailsListFromPostcode);

        HearingCourt hearingCourt = DataFactory.createHearingCourtFromJson(HEARING_COURT_NEWCASTLE);
        when(hearingCourtMapper.from(any())).thenReturn(hearingCourt);

        pilotCourtService.init();

        HearingCourt actualHearingCourt = HearingCourt.builder().build();

        Optional<HearingCourt> birminghamHearingCourt = pilotCourtService.getPilotHearingCourt("BIRMINGHAM");
        if (birminghamHearingCourt.isPresent()) {
            actualHearingCourt = birminghamHearingCourt.get();
        }

        HearingCourt expectedHearingCourt = DataFactory.createHearingCourtFromJson(HEARING_COURT_NEWCASTLE);

        assertEquals(expectedHearingCourt, actualHearingCourt);
    }

    @Test
    void shouldFetchHearingCourtOnDemandIfNotAlreadyExist() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathSingle,
            courtFinderService,
            hearingCourtMapper,
            appInsights
        );

        List<Court> courtDetailsListFromPostcode = DataFactory.createCourtListFromJson(LIST_COURTS_SINGLE_NEWCASTLE);

        when(courtFinderService.getCourtDetailsListFromPostcode(anyString()))
            .thenReturn(courtDetailsListFromPostcode);

        pilotCourtService.init();

        HearingCourt hearingCourt = HearingCourt.builder().name("SAMPLE COURT").build();
        when(hearingCourtMapper.from(any())).thenReturn(hearingCourt);

        HearingCourt actualHearingCourt = pilotCourtService.getPilotHearingCourt("BIRMINGHAM").get();

        assertEquals(hearingCourt, actualHearingCourt);
    }

    @Test
    void shouldReturnAnEmptyOptionalIfCourtFinderReturnsNothing() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathSingle,
            courtFinderService,
            hearingCourtMapper,
            appInsights
        );

        when(courtFinderService.getCourtDetailsListFromPostcode(anyString()))
            .thenReturn(emptyList());

        pilotCourtService.init();

        Optional<HearingCourt> actualHearingCourt = pilotCourtService.getPilotHearingCourt("BIRMINGHAM");

        assertEquals(Optional.empty(), actualHearingCourt);
    }

    @Test
    void shouldThrowValidationErrorForMissingValuesFromCSV() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathInvalid,
            courtFinderService,
            hearingCourtMapper,
            appInsights
        );

        when(courtFinderService.getCourtDetailsListFromPostcode(anyString()))
            .thenReturn(emptyList());

        Assertions.assertThrows(AssertionError.class, pilotCourtService::init);
    }

    @Test
    void shouldReturnListOfCourtIdsForPilot() {
        List<Court> courtDetailsListFromPostcode = DataFactory.createCourtListFromJson(LIST_COURTS_SINGLE_NEWCASTLE);

        when(courtFinderService.getCourtDetailsListFromPostcode(anyString()))
            .thenReturn(courtDetailsListFromPostcode);

        HearingCourt hearingCourt = HearingCourt.builder().name("Newcastle Civil & Family Courts and Tribunals Centre").build();
        when(hearingCourtMapper.from(any())).thenReturn(hearingCourt);

        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathCourtIds,
            courtFinderService,
            hearingCourtMapper,
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
    @DisplayName("Init")
    class InitTests {

        @Test
        void shouldThrowIllegalStateExceptionOnInvalidCSVPath() {
            Assertions.assertThrows(IllegalStateException.class, () ->
                new PilotCourtService(
                    "InvalidPath",
                    courtFinderService,
                    hearingCourtMapper,
                    appInsights
                ).init()
            );
        }

        @Test
        void shouldBuildListOfCourtsFromCSV() {
            PilotCourtService pilotCourtService = new PilotCourtService(
                csvPath,
                courtFinderService,
                hearingCourtMapper,
                appInsights
            );

            pilotCourtService.init();

            Set<String> actualPilotCourtIds = pilotCourtService.getAllPilotCourtIds();

            Set<String> expectedPilotCourtIds = DataFactory.createStringSetFromJson(PILOT_COURT_ALL_COURTS);

            assertEquals(expectedPilotCourtIds, actualPilotCourtIds);
        }
    }

    @Nested
    @DisplayName("Is Pilot Court tests")
    class IsPilotCourtTests {

        private final String pilotCourtName = "Edmonton County Court and Family Court";
        private final String pilotCourtPostcode = "N182TN";
        private final String nonPilotCourtName = "Manchester Civil Justice Centre (Civil and Family Courts)";
        private final String nonPilotCourtPostcode = "M609DJ";
        private PilotCourtService pilotCourtService;

        @BeforeEach
        void setUp() {
            List<Court> courtDetailsListFromPostcode = DataFactory.createCourtListFromJson(LIST_COURTS_SINGLE_NEWCASTLE);

            when(courtFinderService.getCourtDetailsListFromPostcode(anyString()))
                .thenReturn(courtDetailsListFromPostcode);

            when(hearingCourtMapper.from(any()))
                .thenReturn(HearingCourt.builder().name(pilotCourtName).build());
        }

        @Nested
        @DisplayName("Pilot tests")
        class PilotTests {

            @Nested
            @DisplayName("LA Pilot tests")
            class LAPilotTests {

                private final Pilot pilot = Pilot.LA;
                private final String csvPilotPath = csvPathLa;

                @BeforeEach
                void setUp() {

                    pilotCourtService = new PilotCourtService(
                        csvPilotPath,
                        courtFinderService,
                        hearingCourtMapper,
                        appInsights
                    );

                    pilotCourtService.init();
                }

                @Test
                void shouldReturnTrueIfCourtIsPilotCourt() {
                    assertTrue(pilotCourtService.isPilotCourt(pilotCourtName, pilot, LocalDateTime.MAX));
                }

                @Test
                void shouldReturnFalseIfCourtIsNotPilotCourt() {
                    assertFalse(pilotCourtService.isPilotCourt(nonPilotCourtName, pilot, LocalDateTime.MAX));
                }
            }

            @Nested
            @DisplayName("JDDO Pilot tests")
            class JddoAPilotTests {

                private final Pilot pilot = Pilot.JDDO;
                private final String csvPilotPath = csvPathJddo;

                @BeforeEach
                void setUp() {
                    pilotCourtService = new PilotCourtService(
                        csvPilotPath,
                        courtFinderService,
                        hearingCourtMapper,
                        appInsights
                    );

                    pilotCourtService.init();
                }

                @Test
                void shouldReturnTrueIfCourtIsPilotCourt() {
                    assertTrue(pilotCourtService.isPilotCourt(pilotCourtName, pilot, LocalDateTime.MAX));
                }

                @Test
                void shouldReturnFalseIfCourtIsNotPilotCourt() {
                    assertFalse(pilotCourtService.isPilotCourt(nonPilotCourtName, pilot, LocalDateTime.MAX));
                }
            }
        }

        @Nested
        @DisplayName("Court name tests")
        class CourtNameTests {

            @BeforeEach
            void setUp() {
                pilotCourtService = new PilotCourtService(
                    csvPathNames,
                    courtFinderService,
                    hearingCourtMapper,
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
                private final String partialPilotCourtName = "Edmonton";
                private final String partialNonPilotCourtName = "Manchester";

                @Test
                void shouldReturnTrueIfNameIsMatched() {
                    assertTrue(pilotCourtService
                        .isPilotCourt(partialPilotCourtName, Pilot.values()[0], LocalDateTime.MAX));
                }

                @Test
                void shouldReturnFalseIfNameIsNotMatched() {
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
                    csvPathDates,
                    courtFinderService,
                    hearingCourtMapper,
                    appInsights
                );

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
