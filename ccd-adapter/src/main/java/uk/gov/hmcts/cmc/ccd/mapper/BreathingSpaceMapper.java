package uk.gov.hmcts.cmc.ccd.mapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDBreathingSpace;
import uk.gov.hmcts.cmc.ccd.domain.CCDBreathingSpaceType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.BreathingSpaceType;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class BreathingSpaceMapper implements BuilderMapper<CCDCase, BreathingSpace, CCDCase.CCDCaseBuilder> {

    @Override
    public void to(BreathingSpace breathingSpace, CCDCase.CCDCaseBuilder builder) {
        if (breathingSpace == null) {
            return;
        }

        builder
            .breathingSpace(to(breathingSpace));
    }

    private CCDBreathingSpace to(BreathingSpace breathingSpace) {
        return CCDBreathingSpace.builder()
            .bsReferenceNumber(breathingSpace.getBsReferenceNumber())
            .bsType(toCCDBreathingSpaceType(breathingSpace.getBsType()))
            .bsEnteredDate(breathingSpace.getBsEnteredDate())
            .bsLiftedDate(breathingSpace.getBsLiftedDate())
            .bsEnteredDateByInsolvencyTeam(breathingSpace.getBsEnteredDateByInsolvencyTeam())
            .bsLiftedDateByInsolvencyTeam(breathingSpace.getBsLiftedDateByInsolvencyTeam())
            .bsExpectedEndDate(breathingSpace.getBsExpectedEndDate())
            .build();
    }

    @Override
    public BreathingSpace from(CCDCase ccdCase) {
        if (ccdCase.getBreathingSpace() == null
        ) {
            return null;
        }
        CCDBreathingSpace ccdBreathingSpace = ccdCase.getBreathingSpace();

        return new BreathingSpace(
            ccdBreathingSpace.getBsReferenceNumber(),
            fromCCDBreathingSpaceType(ccdBreathingSpace.getBsType()),
            ccdBreathingSpace.getBsEnteredDate(),
            ccdBreathingSpace.getBsLiftedDate(),
            ccdBreathingSpace.getBsEnteredDateByInsolvencyTeam(),
            ccdBreathingSpace.getBsLiftedDateByInsolvencyTeam(),
            ccdBreathingSpace.getBsExpectedEndDate());
    }

    private CCDBreathingSpaceType toCCDBreathingSpaceType(BreathingSpaceType breathingSpaceType) {
        return CCDBreathingSpaceType.valueOf(breathingSpaceType.name());
    }

    private BreathingSpaceType fromCCDBreathingSpaceType(CCDBreathingSpaceType ccdBreathingSpaceType) {
        return BreathingSpaceType.valueOf(ccdBreathingSpaceType.name());
    }
}
