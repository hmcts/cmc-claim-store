package uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Getter
public class CCDUnavailableDate extends CollectionId {

    private LocalDate unavailableDate;

    @Builder
    public CCDUnavailableDate(String id, LocalDate unavailableDate) {
        super(id);
        this.unavailableDate = unavailableDate;
    }
}
