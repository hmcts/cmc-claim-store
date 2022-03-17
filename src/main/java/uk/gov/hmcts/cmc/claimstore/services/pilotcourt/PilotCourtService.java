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
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.containers.CourtFinderContainer;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.CourtDetails;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.Court;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtAddress;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@Service
public class PilotCourtService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int PILOT_COURT_GO_LIVE_TIME = 11;

    private Map<String, PilotCourt> pilotCourts;
    private final CourtFinderApi courtFinderApi;
    private final String dataSource;
    private final AppInsights appInsights;

    public static final String OTHER_COURT_ID = "OTHER";

    public PilotCourtService(@Value("${pilot-courts.datafile}") String dataSource,
                             CourtFinderApi courtFinderApi,
                             AppInsights appInsights) {
        this.dataSource = dataSource;
        this.courtFinderApi = courtFinderApi;
        this.appInsights = appInsights;
    }

    @PostConstruct
    public void init() {

        try {
            String data = ResourceReader.readString(dataSource);

            Map<String, PilotCourt> pilotCourtMap = buildPilotCourts(data);
            this.pilotCourts = Collections.unmodifiableMap(pilotCourtMap);

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

        if (pilotCourt.getHearingCourt().isEmpty()) {
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

        Map<String, PilotCourt> pilotCourtMap = new HashMap<>();
        while (iterator.hasNext()) {
            CSVRecord csvRecord = iterator.next();

            if (csvRecord.size() != PilotCourtCSVHeader.values().length) {
                throw new AssertionError(String.format("Pilot court configuration for %s is expected to have %d values",
                    csvRecord,
                    PilotCourtCSVHeader.values().length));
            }

            String id = csvRecord.get(PilotCourtCSVHeader.ID.ordinal()).toUpperCase();
            String postcode = csvRecord.get(PilotCourtCSVHeader.POSTCODE.ordinal());

            Map<Pilot, LocalDateTime> pilots = getPilots(csvRecord);

            try {
                Optional<HearingCourt> pilotCourt = getCourt(postcode);
                pilotCourtMap.put(id, new PilotCourt(id, postcode, pilotCourt.orElse(null), pilots));

            } catch (FeignException e) {
                logger.error("Failed to get address from Court Finder API", e);
                appInsights.trackEvent(AppInsightsEvent.COURT_FINDER_API_FAILURE, "Court postcode", postcode);
                pilotCourtMap.put(id, new PilotCourt(id, postcode, null, pilots));
            }
        }

        return pilotCourtMap;
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

        CourtFinderResponse courtFinderResponse = courtFinderApi.findMoneyClaimCourtByPostcode(postcode);

        if (courtFinderResponse == null || courtFinderResponse.getCourts() == null || courtFinderResponse.getCourts().isEmpty()) {
            return Optional.empty();
        }

        List<Court> courts = courtFinderResponse.getCourts();

        uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court court = new CourtFinderContainer(courtFinderApi).getCourtFromCourtFinderResponse(courts.get(0));

        CourtDetails courtDetails = courtFinderApi.getCourtDetailsFromNameSlug(court.getSlug());

        CCDAddress ccdAddress = null;

        if (courtDetails != null && !courtDetails.getAddresses().isEmpty()) {
            ccdAddress = getCCDAddress(courtDetails.getAddresses().get(0));
        }

        HearingCourt hearingCourt = HearingCourt.builder()
            .name(court.getName())
            .address(ccdAddress)
            .build();

        return Optional.of(hearingCourt);
    }

    /**
     * TODO : DESCRIPTION
     *
     * @param courtAddress
     * @param ccdAddress
     */
    private CCDAddress getCCDAddress(CourtAddress courtAddress) {
        CCDAddress ccdAddress = CCDAddress.builder()
            .postCode(courtAddress.getPostcode())
            .postTown(courtAddress.getTown())
            .build();

        if (!courtAddress.getAddressLines().isEmpty()) {
            ccdAddress.setAddressLine1(courtAddress.getAddressLines().get(0));

            int addressLineCount = courtAddress.getAddressLines().size();

            if (addressLineCount >= 2) {
                ccdAddress.setAddressLine2(courtAddress.getAddressLines().get(1));

                if (addressLineCount >= 3) {
                    ccdAddress.setAddressLine3(courtAddress.getAddressLines().get(2));
                }
            }
        }

        return ccdAddress;
    }

    /**
     * TODO : DESCRIPTION
     * @param hearingCourt
     * @return {@linkplain String}
     */
    public String getPilotCourtId(HearingCourt hearingCourt) {

        return pilotCourts.keySet()
            .stream()
            .filter(pilotCourtId ->  getPilotHearingCourt(pilotCourtId).filter(hearingCourt::equals).isPresent())
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Supplied Hearing Court is not a pilot court"));
    }
}
