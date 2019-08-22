package uk.gov.hmcts.cmc.domain.models.orders;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.time.LocalDate;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class Direction extends CollectionId {

    private DirectionParty directionParty;
    private DirectionType directionType;
    private DirectionHeaderType directionHeaderType;
    private LocalDate directionActionedDate;
    private String directionComment;
    private List<String> extraDocuments;
    private List<String> expertReports;

    @Builder
    public Direction(
        String id,
        DirectionParty directionParty,
        DirectionType directionType,
        DirectionHeaderType directionHeaderType,
        LocalDate directionActionedDate,
        String directionComment,
        List<String> extraDocuments,
        List<String> expertReports
    ) {
        super(id);
        this.directionParty = directionParty;
        this.directionType = directionType;
        this.directionHeaderType = directionHeaderType;
        this.directionActionedDate = directionActionedDate;
        this.directionComment = directionComment;
        this.extraDocuments = extraDocuments;
        this.expertReports = expertReports;
    }
}
