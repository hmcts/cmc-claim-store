package uk.gov.hmcts.cmc.claimstore.services.pilotcourt;

import feign.FeignException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourtMapper;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourt;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtCSVHeader;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@Service
public class PilotCourtService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int PILOT_COURT_GO_LIVE_TIME = 11;

    private Map<String, PilotCourt> pilotCourts;
    private final CourtFinderApi courtFinderApi;
    private final HearingCourtMapper hearingCourtMapper;
    private final String dataSource;
    private final AppInsights appInsights;

    public static final String OTHER_COURT_ID = "OTHER";

    public PilotCourtService(@Value("${pilot-courts.datafile}") String dataSource,
                             CourtFinderApi courtFinderApi,
                             HearingCourtMapper hearingCourtMapper,
                             AppInsights appInsights) {
        this.dataSource = dataSource;
        this.hearingCourtMapper = hearingCourtMapper;
        this.courtFinderApi = courtFinderApi;
        this.appInsights = appInsights;
    }

    @PostConstruct
    public void init() {

        try {
            String data = ResourceReader.readString(dataSource);

            Map<String, PilotCourt> pilotCourts = buildPilotCourts(data);
            this.pilotCourts = Collections.unmodifiableMap(pilotCourts);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Set<String> getAllPilotCourtIds() {
        return pilotCourts.keySet();
    }

    public Set<HearingCourt> getPilotHearingCourts(Pilot pilot, LocalDateTime claimCreated) {

        return pilotCourts.values()
            .stream()
            .filter(p -> p.isActivePilotCourt(pilot, claimCreated))
            .map(PilotCourt::getId)
            .map(this::getPilotHearingCourt)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    public Optional<HearingCourt> getPilotHearingCourt(String pilotCourtId) {
        if (!pilotCourts.containsKey(pilotCourtId)) {
            throw new IllegalArgumentException(String.format("Supplied pilotCourtId '%s' not found", pilotCourtId));
        }

        PilotCourt pilotCourt = pilotCourts.get(pilotCourtId);

        if (!pilotCourt.getHearingCourt().isPresent()) {
            Optional<HearingCourt> court = getCourt(pilotCourt.getPostcode());
            pilotCourt.setHearingCourt(court.orElse(null));
        }

        return pilotCourt.getHearingCourt();
    }

    public boolean isPilotCourt(String courtName, Pilot pilot, LocalDateTime claimCreated) {
        if (courtName == null) {
            return false;
        }

        return pilotCourts.values()
            .stream()
            .anyMatch(pilotCourt -> checkPilotCourt(pilotCourt, courtName, pilot, claimCreated));
    }

    private boolean checkPilotCourt(PilotCourt pilotCourt, String courtName, Pilot pilot, LocalDateTime claimCreated) {
        String pilotCourtName = getPilotHearingCourt(pilotCourt.getId())
            .orElseThrow(() -> new IllegalStateException("Hearing Court not found for " + pilotCourt.getId()))
            .getName();

        return StringUtils.containsIgnoreCase(pilotCourtName, courtName)
            && pilotCourt.isActivePilotCourt(pilot, claimCreated);
    }

    private Map<String, PilotCourt> buildPilotCourts(String data) throws IOException {
        CSVParser parse = CSVParser.parse(data, CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parse.iterator();

        Map<String, PilotCourt> pilotCourts = new HashMap<>();
        while (iterator.hasNext()) {
            CSVRecord csvRecord = iterator.next();

            if (csvRecord.size() != PilotCourtCSVHeader.values().length) {
                throw new AssertionError(String.format("Pilot court configuration for %s is expected to have %d values",
                    csvRecord.toString(),
                    PilotCourtCSVHeader.values().length));
            }

            String id = csvRecord.get(PilotCourtCSVHeader.ID.ordinal()).toUpperCase();
            String postcode = csvRecord.get(PilotCourtCSVHeader.POSTCODE.ordinal());

            Map<Pilot, LocalDateTime> pilots = getPilots(csvRecord);

            try {
                Optional<HearingCourt> pilotCourt = getCourt(postcode);
                pilotCourts.put(id, new PilotCourt(id, postcode, pilotCourt.orElse(null), pilots));

            } catch (FeignException e) {
                logger.error("Failed to get address from Court Finder API", e);
                appInsights.trackEvent(AppInsightsEvent.COURT_FINDER_API_FAILURE, "Court postcode", postcode);
                pilotCourts.put(id, new PilotCourt(id, postcode, null, pilots));
            }
        }

        return pilotCourts;
    }

    private Map<Pilot, LocalDateTime> getPilots(CSVRecord csvRecord) {
        return Arrays.stream(PilotCourtCSVHeader.values())
            .filter(pilotCourtCSVHeader -> pilotCourtCSVHeader.getPilot().isPresent()
                && isActivePilot(csvRecord.get(pilotCourtCSVHeader.ordinal())))
            .map(pilotCourtCSVHeader ->
                new Object[]{
                    pilotCourtCSVHeader.getPilot().get(),
                    LocalDate.parse(csvRecord.get(pilotCourtCSVHeader.ordinal()), ISO_LOCAL_DATE)
                        .atTime(PILOT_COURT_GO_LIVE_TIME, 0)

                }
            )
            .collect(Collectors.toMap(e -> (Pilot)e[0], e -> (LocalDateTime) e[1]));
    }

    /**
     *  Valid configuration for a pilot trigger date is either a date in ISO-8601 format (yyyy-MM-dd) or false.
     *
     * @param pilotConfig String holding pilot configuration.
     * @return true if date exists and is valid, false if configuration is false.
     * @throws IllegalArgumentException configuration is neither a date in ISO-8601 format (yyyy-MM-dd) or false.
     */
    private boolean isActivePilot(String pilotConfig) {
        try {
            LocalDate.parse(pilotConfig, ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            if (!pilotConfig.equals("false")) {
                throw new IllegalArgumentException(String.format("Expecting configuration to either be a valid date or "
                    + "'false' but got %s", pilotConfig));
            }
            return false;
        }
        return true;
    }

    private Optional<HearingCourt> getCourt(String postcode) {
        return courtFinderApi.findMoneyClaimCourtByPostcode(postcode)
            .stream()
            .findFirst()
            .map(hearingCourtMapper::from);
    }

    public String getPilotCourtId(HearingCourt hearingCourt) {

        return pilotCourts.keySet()
            .stream()
            .filter(pilotCourtId ->  getPilotHearingCourt(pilotCourtId).filter(hearingCourt::equals).isPresent())
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Supplied Hearing Court is not a pilot court"));
    }
}
