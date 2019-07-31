package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.CourtLocationType;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;

public class SampleHearingLocation {

    private SampleHearingLocation() {
        // Do Nothing constructor
    }

    public static final HearingLocation defaultHearingLocation = HearingLocation.builder()
        .courtName("A Court")
        .hearingLocationSlug("a-court")
        .courtAddress(SampleAddress.builder().build())
        .locationOption(CourtLocationType.ALTERNATE_COURT)
        .build();

}
