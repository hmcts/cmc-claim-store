package uk.gov.hmcts.cmc.claimstore.services.pilotcourt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Assertions;
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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Nested
    @DisplayName("Init")
    class InitTests {

        @Test
        void shouldThrowIllegalStateExceptionOnInvalidCSVPath() {
            Assertions.assertThrows(IllegalStateException.class, () -> {
                new PilotCourtService(
                    "InvalidPath",
                    courtFinderApi,
                    hearingCourtMapper,
                    appInsights
                ).init();
            });
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
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            PilotCourtService pilotCourtService = new PilotCourtService(
                csvPathSingle,
                courtFinderApi,
                hearingCourtMapper,
                appInsights
            );
            pilotCourtService.init();
            pilotCourtService.getHearingCourt("UNKNOWN_ID");
        });
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

        HearingCourt actualHearingCourt = pilotCourtService.getHearingCourt("BIRMINGHAM");

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

        HearingCourt actualHearingCourt = pilotCourtService.getHearingCourt("BIRMINGHAM");

        assertEquals(hearingCourt, actualHearingCourt);

    }

    @Test
    void shouldReturnABlankHearingCourtIfCourtFinderReturnsNothing() {
        PilotCourtService pilotCourtService = new PilotCourtService(
            csvPathSingle,
            courtFinderApi,
            hearingCourtMapper,
            appInsights
        );

        when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString())).thenReturn(ImmutableList.of());
        pilotCourtService.init();

        HearingCourt actualHearingCourt = pilotCourtService.getHearingCourt("BIRMINGHAM");

        assertEquals(HearingCourt.builder().build(), actualHearingCourt);

    }

}
