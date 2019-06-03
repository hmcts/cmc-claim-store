package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDUnavailableDate;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;

@Component
public class UnavailableDateMapper {

    public CCDCollectionElement<CCDUnavailableDate> to(UnavailableDate unavailableDate) {

        CCDUnavailableDate.CCDUnavailableDateBuilder builder = CCDUnavailableDate.builder();

        return CCDCollectionElement.<CCDUnavailableDate>builder()
            .value(
                builder.unavailableDate(unavailableDate.getUnavailableDate())
                .build())
            .id(unavailableDate.getId())
            .build();
    }

    public UnavailableDate from(CCDCollectionElement<CCDUnavailableDate> ccdUnavailableDate) {

        return UnavailableDate
            .builder()
            .id(ccdUnavailableDate.getId())
            .unavailableDate(ccdUnavailableDate.getValue().getUnavailableDate())
            .build();
    }
}
