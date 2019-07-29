package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;

import java.time.LocalDate;

@Component
public class UnavailableDateMapper implements Mapper<CCDCollectionElement<LocalDate>, UnavailableDate> {

    @Override
    public CCDCollectionElement<LocalDate> to(UnavailableDate unavailableDate) {
        if (unavailableDate == null) {
            return null;
        }
        return CCDCollectionElement.<LocalDate>builder()
            .value(unavailableDate.getUnavailableDate())
            .id(unavailableDate.getId())
            .build();
    }

    @Override
    public UnavailableDate from(CCDCollectionElement<LocalDate> ccdUnavailableDate) {
        if (ccdUnavailableDate == null) {
            return null;
        }
        return UnavailableDate
            .builder()
            .id(ccdUnavailableDate.getId())
            .unavailableDate(ccdUnavailableDate.getValue())
            .build();
    }
}
