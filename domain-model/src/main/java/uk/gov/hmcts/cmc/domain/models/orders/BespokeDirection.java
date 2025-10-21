package uk.gov.hmcts.cmc.domain.models.orders;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BespokeDirection extends CollectionId {
    private final DirectionParty beSpokeDirectionFor;
    private final LocalDate beSpokeDirectionDatetime;
    private final String beSpokeDirectionExplain;

    @Builder
    public BespokeDirection(
        String id,
        DirectionParty beSpokeDirectionFor,
        LocalDate beSpokeDirectionDatetime,
        String beSpokeDirectionExplain
    ) {
        super(id);
        this.beSpokeDirectionFor = beSpokeDirectionFor;
        this.beSpokeDirectionDatetime = beSpokeDirectionDatetime;
        this.beSpokeDirectionExplain = beSpokeDirectionExplain;
    }
}
