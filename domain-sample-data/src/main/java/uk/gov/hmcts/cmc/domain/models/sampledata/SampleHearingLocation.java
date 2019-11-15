package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.CourtLocationType;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt;

public class SampleHearingLocation {

    public static final HearingLocation defaultHearingLocation = HearingLocation.builder()
        .courtName("A Court")
        .hearingLocationSlug("a-court")
        .courtAddress(SampleAddress.builder().build())
        .locationOption(CourtLocationType.ALTERNATE_COURT)
        .build();

    public static final HearingLocation pilotHearingLocation = HearingLocation.builder()
        .courtName(PilotCourt.BIRMINGHAM.getName())
        .hearingLocationSlug("a-court")
        .courtAddress(SampleAddress.builder().build())
        .locationOption(CourtLocationType.SUGGESTED_COURT)
        .build();

    private SampleHearingLocation() {
        // Do Nothing constructor
    }

}
