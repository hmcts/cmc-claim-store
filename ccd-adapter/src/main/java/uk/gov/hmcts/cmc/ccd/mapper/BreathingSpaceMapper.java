package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDBreathingSpace;
import uk.gov.hmcts.cmc.ccd.domain.CCDBreathingSpaceType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.BreathingSpaceType;

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
        if (ccdCase.getBreathingSpace() == null || ccdCase.getBreathingSpace().getBsType() == null
        ) {
            return null;
        }
        CCDBreathingSpace ccdBreathingSpace = ccdCase.getBreathingSpace();

        return new BreathingSpace(
            ccdBreathingSpace.getBsReferenceNumber(),
            fromCCDBreathingSpaceType(ccdBreathingSpace != null && ccdBreathingSpace.getBsType() != null
                ? ccdBreathingSpace.getBsType() : null),
            ccdBreathingSpace.getBsEnteredDate(),
            ccdBreathingSpace.getBsLiftedDate(),
            ccdBreathingSpace.getBsEnteredDateByInsolvencyTeam(),
            ccdBreathingSpace.getBsLiftedDateByInsolvencyTeam(),
            ccdBreathingSpace.getBsExpectedEndDate(),
            ccdBreathingSpace.getBsLiftedFlag());
    }

    private CCDBreathingSpaceType toCCDBreathingSpaceType(BreathingSpaceType breathingSpaceType) {
        return CCDBreathingSpaceType.valueOf(breathingSpaceType.name());
    }

    private BreathingSpaceType fromCCDBreathingSpaceType(CCDBreathingSpaceType ccdBreathingSpaceType) {
        return BreathingSpaceType.valueOf(ccdBreathingSpaceType.name());
    }
}
