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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

@Service
public class PilotCourtService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, PilotCourt> pilotCourts;
    private final CourtFinderApi courtFinderApi;
    private final HearingCourtMapper hearingCourtMapper;
    private final String dataSource;
    private final AppInsights appInsights;

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

    public Set<HearingCourt> getPilotHearingCourts(Pilot pilot) {

        return pilotCourts.values()
            .stream()
            .filter(p -> p.getPilots().contains(pilot))
            .map(PilotCourt::getId)
            .map(this::getPilotHearingCourt)
            .collect(Collectors.toSet());
    }

    public HearingCourt getPilotHearingCourt(String pilotCourtId) {
        if (!pilotCourts.containsKey(pilotCourtId)) {
            throw new IllegalArgumentException(String.format("Supplied pilotCourtId '%s' not found", pilotCourtId));
        }

        PilotCourt pilotCourt = pilotCourts.get(pilotCourtId);

        return pilotCourt.getHearingCourt()
            .orElseGet(() -> {
                Optional<HearingCourt> court = getCourt(pilotCourt.getPostcode());
                pilotCourt.setHearingCourt(court.orElse(null));

                return court.orElseGet(() -> HearingCourt.builder().build());
            }
        );
    }

    public boolean isPilotCourt(String courtName, Pilot pilot) {
        if (courtName == null) {
            return false;
        }

        return pilotCourts.values()
            .stream()
            .anyMatch(pilotCourt -> checkPilotCourt(pilotCourt, courtName, pilot));
    }

    private boolean checkPilotCourt(PilotCourt pilotCourt, String courtName, Pilot pilot) {
        String pilotCourtName = getPilotHearingCourt(pilotCourt.getId())
            .getName()
            .toLowerCase();

        return StringUtils.containsIgnoreCase(pilotCourtName, courtName) && pilotCourt.getPilots().contains(pilot);
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

            Set<Pilot> pilots = getPilots(csvRecord);

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

    private Set<Pilot> getPilots(CSVRecord next) {
        Set<Pilot> pilots = new HashSet<>();
        if (Boolean.parseBoolean(next.get(PilotCourtCSVHeader.LA_PILOT.ordinal()))) {
            pilots.add(Pilot.LA);
        }
        if (Boolean.parseBoolean(next.get(PilotCourtCSVHeader.JDDO_PILOT.ordinal()))) {
            pilots.add(Pilot.JDDO);
        }
        return pilots;
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
            .filter(pilotCourtId ->  getPilotHearingCourt(pilotCourtId).equals(hearingCourt))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Supplied Hearing Court is not a pilot court"));
    }
}
