package uk.gov.hmcts.cmc.domain.models.orders;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
@EqualsAndHashCode
@Getter
public class DirectionOrder {
    private final List<Direction> directions = new ArrayList<>();
    private List<String> extraDocUploadList;
    private YesNoOption paperDetermination;
    private String newRequestedCourt;
    private String preferredDQCourt;
    private String preferredCourtObjectingReason;
    private PilotCourt hearingCourt;
    private Address hearingCourtAddress;
    private HearingDurationType estimatedHearingDuration;
    private YesNoOption expertReportPermissionGivenToClaimant;
    private YesNoOption expertReportPermissionGivenToDefendant;
    private YesNoOption expertReportPermissionAskedByClaimant;
    private YesNoOption expertReportPermissionAskedByDefendant;
    private List<String> expertReportInstructionsForClaimant;
    private List<String> expertReportInstructionsForDefendant;
    private LocalDateTime createdOn;

    public void addDirection(Direction direction) {
        directions.add(direction);
    }

    public Optional<Direction> getDirection(DirectionType directionType) {
        return directions.stream()
            .filter(direction -> direction.getDirectionType().equals(directionType))
            .findFirst();
    }
}
