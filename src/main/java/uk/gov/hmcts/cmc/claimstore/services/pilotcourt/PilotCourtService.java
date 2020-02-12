package uk.gov.hmcts.cmc.claimstore.services.pilotcourt;

import feign.FeignException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourtMapper;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;

@Service
public class PilotCourtService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, PilotCourt> pilotCourts;
    private final CourtFinderApi courtFinderApi;
    private final HearingCourtMapper hearingCourtMapper;
    private final String dataSource;

    public PilotCourtService(@Value("${pilot-courts.datafile}") String dataSource,
                             CourtFinderApi courtFinderApi,
                             HearingCourtMapper hearingCourtMapper) {
        this.dataSource = dataSource;
        this.hearingCourtMapper = hearingCourtMapper;
        this.courtFinderApi = courtFinderApi;
    }

    @PostConstruct
    public void init() {

        try {
            String data = ResourceReader.readString(dataSource);
            CSVParser parse = CSVParser.parse(data, CSVFormat.DEFAULT);
            Iterator<CSVRecord> iterator = parse.iterator();

            Map<String, PilotCourt> pilotCourts = buildPilotCourts(iterator);
            this.pilotCourts = Collections.unmodifiableMap(pilotCourts);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Set<String> getAllPilotCourtIds() {
        return pilotCourts.keySet();
    }

    public HearingCourt getHearingCourt(String pilotCourtId) {
        if (!pilotCourts.containsKey(pilotCourtId)) {
            throw new IllegalArgumentException(String.format("Supplied pilotCourtId '%s' not found", pilotCourtId));
        }

        PilotCourt pilotCourt = pilotCourts.get(pilotCourtId);

        return pilotCourt.getHearingCourt().orElseGet(() -> {
                Optional<HearingCourt> court = getCourt(pilotCourt.getPostcode());
                pilotCourt.setHearingCourt(court);

                return court.orElseGet(() -> HearingCourt.builder().build());
            }
        );
    }

    public boolean isPilotCourt(String courtName) {
        if (courtName == null) {
            return false;
        }

        return pilotCourts.keySet().stream().anyMatch(pilotCourt -> courtName.toUpperCase().contains(pilotCourt));
    }

    private Map<String, PilotCourt> buildPilotCourts(Iterator<CSVRecord> iterator) {

        Map<String, PilotCourt> pilotCourts = new HashMap<>();
        while (iterator.hasNext()) {
            CSVRecord next = iterator.next();

            String id = next.get(PilotCourtCSVHeader.ID.ordinal()).toUpperCase();
            String postcode = next.get(PilotCourtCSVHeader.POSTCODE.ordinal());

            try {
                Optional<HearingCourt> pilotCourt = getCourt(postcode);
                pilotCourts.put(id, new PilotCourt(id, postcode, pilotCourt));

            } catch (FeignException e) {
                logger.error("Failed to get address from Court Finder API", e);
                pilotCourts.put(id, new PilotCourt(id, postcode, Optional.empty()));
            }
        }

        return pilotCourts;
    }

    private Optional<HearingCourt> getCourt(String postcode) {
        return courtFinderApi.findMoneyClaimCourtByPostcode(postcode)
            .stream()
            .findFirst()
            .map(hearingCourtMapper::from);
    }
}
