package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDUnEmployed;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.UnEmployed;

@Component
public class UnEmployedMapper implements Mapper<CCDUnEmployed, UnEmployed> {

    @Override
    public CCDUnEmployed to(UnEmployed unEmployed) {
        return CCDUnEmployed.builder()
            .type(unEmployed.getType())
            .noOfMonths(unEmployed.getNoOfMonths().orElse(0))
            .noOfYears(unEmployed.getNoOfYears().orElse(0))
            .build();
    }

    @Override
    public UnEmployed from(CCDUnEmployed ccdUnEmployed) {
        if (ccdUnEmployed == null) {
            return null;
        }
        return new UnEmployed(
            ccdUnEmployed.getType(),
            ccdUnEmployed.getNoOfYears(),
            ccdUnEmployed.getNoOfMonths()
        );
    }
}
