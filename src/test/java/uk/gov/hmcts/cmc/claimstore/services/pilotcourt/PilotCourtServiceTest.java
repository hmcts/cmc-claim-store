package uk.gov.hmcts.cmc.claimstore.services.pilotcourt;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourtMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PilotCourtServiceTest {

    @Mock
    private AppInsights appInsights;

    @Mock
    private CourtFinderApi courtFinderApi;

    @Mock
    private HearingCourtMapper hearingCourtMapper;

    private String csvPath = "/pilot-court/pilot-courts.csv";
    private String csvPathSingle = "/pilot-court/pilot-courts-single.csv";
    private String csvPathInvalid = "/pilot-court/pilot-courts-invalid.csv";
    private String csvPathLa = "/pilot-court/pilot-courts-LA.csv";
    private String csvPathJddo = "/pilot-court/pilot-courts-JDDO.csv";
    private String csvPathNames = "/pilot-court/pilot-courts-names.csv";
    private String csvPathDates = "/pilot-court/pilot-courts-dates.csv";
    private String csvPathCourtIds = "/pilot-court/pilot-courts-court-ids.csv";

    @Nested
    @DisplayName("Init")
    class InitTests {

        @Test
        void shouldThrowIllegalStateExceptionOnInvalidCSVPath() {
            Assertions.assertThrows(IllegalStateException.class, () ->
                new PilotCourtService(
                    "InvalidPath",
                    courtFinderApi,
                    hearingCourtMapper,
                    appInsights
                ).init()
            );
        }

        @Test
        void shouldBuildListOfCourtsFromCSV() {
            PilotCourtService pilotCourtService = new PilotCourtService(
                csvPath,
                courtFinderApi,
                hearingCourtMapper,
                appInsights
            );

            pilotCourtService.init();

            Set<String> allPilotCourtIds = pilotCourtService.getAllPilotCourtIds();

            assertEquals(4, allPilotCourtIds.size());
            assertTrue(allPilotCourtIds.contains("EDMONTON"));
            assertTrue(allPilotCourtIds.contains("MANCHESTER"));
            assertTrue(allPilotCourtIds.contains("BIRMINGHAM"));
            assertTrue(allPilotCourtIds.contains("CLERKENWELL"));
        }
    }

    @Test
    void shouldThrowRIllegalArgumentExceptionOnUnknownCourtId() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathSingle,
            courtFinderApi,
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
            courtFinderApi,
            hearingCourtMapper,
            appInsights
        );

        Court court = Court.builder().build();
        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
            .thenReturn(ImmutableList.of(court));

        HearingCourt hearingCourt = HearingCourt.builder().name("SAMPLE COURT").build();
        when(hearingCourtMapper.from(eq(court))).thenReturn(hearingCourt);
        pilotCourtService.init();

        HearingCourt actualHearingCourt = pilotCourtService.getPilotHearingCourt("BIRMINGHAM").get();

        assertEquals(hearingCourt, actualHearingCourt);

    }

    @Test
    void shouldFetchHearingCourtOnDemandIfNotAlreadyExist() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathSingle,
            courtFinderApi,
            hearingCourtMapper,
            appInsights
        );

        //Simulate courtfinder being down on init
        Court court = Court.builder().build();
        Request request = Request.create(Request.HttpMethod.GET, "URL", ImmutableMap.of(), Request.Body.empty());
        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString())).thenThrow(FeignException.errorStatus("",
            Response.builder().request(request).build()))
            .thenReturn(ImmutableList.of(court));

        pilotCourtService.init();

        HearingCourt hearingCourt = HearingCourt.builder().name("SAMPLE COURT").build();
        when(hearingCourtMapper.from(eq(court))).thenReturn(hearingCourt);

        HearingCourt actualHearingCourt = pilotCourtService.getPilotHearingCourt("BIRMINGHAM").get();

        assertEquals(hearingCourt, actualHearingCourt);

    }

    @Test
    void shouldReturnAnEmptyOptionalIfCourtFinderReturnsNothing() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathSingle,
            courtFinderApi,
            hearingCourtMapper,
            appInsights
        );

        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString())).thenReturn(ImmutableList.of());
        pilotCourtService.init();

        Optional<HearingCourt> actualHearingCourt = pilotCourtService.getPilotHearingCourt("BIRMINGHAM");

        assertEquals(Optional.empty(), actualHearingCourt);
    }

    @Test
    void shouldThrowValidationErrorForMissingValuesFromCSV() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathInvalid,
            courtFinderApi,
            hearingCourtMapper,
            appInsights
        );

        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString())).thenReturn(ImmutableList.of());
        Assertions.assertThrows(AssertionError.class, pilotCourtService::init);
    }

    @Test
    void shouldReturnListOfCourtIdsForPilot() {

        Court edmontonCourt = Court.builder().name("EDMONTON").build();
        when(courtFinderApi.findMoneyClaimCourtByPostcode(eq("N182TN"))).thenReturn(ImmutableList.of(edmontonCourt));
        when(hearingCourtMapper.from(eq(edmontonCourt))).thenReturn(HearingCourt.builder().name("EDMONTON").build());

        Court manchesterCourt = Court.builder().name("MANCHESTER").build();
        when(courtFinderApi.findMoneyClaimCourtByPostcode(eq("M609DJ"))).thenReturn(ImmutableList.of(manchesterCourt));
        when(hearingCourtMapper.from(eq(manchesterCourt)))
            .thenReturn(HearingCourt.builder().name("MANCHESTER").build());

        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathCourtIds,
            courtFinderApi,
            hearingCourtMapper,
            appInsights
        );
        pilotCourtService.init();

        LocalDateTime claimCreatedDate = LocalDateTime.of(2019, 9, 9, 11, 0, 0);
        Pilot pilot = Pilot.LA;

        Set<String> pilotHearingCourtNames = pilotCourtService.getPilotHearingCourts(pilot, claimCreatedDate)
                .stream()
                .map(HearingCourt::getName)
                .collect(Collectors.toSet());

        assertEquals(2, pilotHearingCourtNames.size());
        assertTrue(pilotHearingCourtNames.contains("EDMONTON"));
        assertTrue(pilotHearingCourtNames.contains("MANCHESTER"));
        assertFalse(pilotHearingCourtNames.contains("BIRMINGHAM"));
        assertFalse(pilotHearingCourtNames.contains("CLERKENWELL"));

    }

    @Nested
    @DisplayName("Is Pilot Court tests")
    class IsPilotCourtTests {

        private PilotCourtService pilotCourtService;

        private final String pilotCourtName = "Edmonton County Court and Family Court";
        private final String pilotCourtPostcode = "N182TN";

        private final String nonPilotCourtName = "Manchester Civil Justice Centre (Civil and Family Courts)";
        private final String nonPilotCourtPostcode = "M609DJ";

        @BeforeEach
        void setUp() {

            Court pilotCourt = Court.builder().name(pilotCourtName).build();
            when(courtFinderApi.findMoneyClaimCourtByPostcode(eq(pilotCourtPostcode)))
                .thenReturn(ImmutableList.of(pilotCourt));

            when(hearingCourtMapper.from(eq(pilotCourt)))
                .thenReturn(HearingCourt.builder().name(pilotCourtName).build());

        }

        @Nested
        @DisplayName("Pilot tests")
        class PilotTests {

            @BeforeEach
            void setUp() {

                Court nonPilotCourt = Court.builder().name(nonPilotCourtName).build();
                when(courtFinderApi.findMoneyClaimCourtByPostcode(eq(nonPilotCourtPostcode)))
                    .thenReturn(ImmutableList.of(nonPilotCourt));

                when(hearingCourtMapper.from(eq(nonPilotCourt)))
                    .thenReturn(HearingCourt.builder().name(nonPilotCourtName).build());
            }

            @Nested
            @DisplayName("LA Pilot tests")
            class LAPilotTests {

                private final Pilot pilot = Pilot.LA;
                private final String csvPilotPath = csvPathLa;

                @BeforeEach
                void setUp() {

                    pilotCourtService = new PilotCourtService(
                        csvPilotPath,
                        courtFinderApi,
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
                        courtFinderApi,
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
                    courtFinderApi,
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
                    courtFinderApi,
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
