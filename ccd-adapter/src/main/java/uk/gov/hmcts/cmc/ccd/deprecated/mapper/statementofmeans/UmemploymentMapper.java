package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDUnemployed;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDUnemployment;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployed;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption.valueOf;

@Component
public class UmemploymentMapper implements Mapper<CCDUnemployment, Unemployment> {

    @Override
    public CCDUnemployment to(Unemployment unemployment) {

        CCDUnemployment.CCDUnemploymentBuilder builder = CCDUnemployment.builder()
            .retired(valueOf(unemployment.isRetired()))
            .other(unemployment.getOther().orElse(null));

        unemployment.getUnemployed()
            .ifPresent(unemployed -> builder.unemployed(CCDUnemployed.builder()
                    .numberOfYears(unemployed.getNumberOfYears())
                    .numberOfMonths(unemployed.getNumberOfMonths())
                    .build()
                )
            );

        return builder.build();
    }

    @Override
    public Unemployment from(CCDUnemployment ccdUnemployment) {
        if (ccdUnemployment == null) {
            return null;
        }

        Unemployment.UnemploymentBuilder builder = Unemployment.builder()
            .retired(ccdUnemployment.getRetired() != null ? ccdUnemployment.getRetired().toBoolean() : false)
            .other(ccdUnemployment.getOther());

        CCDUnemployed ccdUnemployed = ccdUnemployment.getUnemployed();
        if (ccdUnemployed != null) {
            builder.unemployed(Unemployed.builder()
                .numberOfYears(ccdUnemployed.getNumberOfYears())
                .numberOfMonths(ccdUnemployed.getNumberOfMonths())
                .build()
            );
        }
        return builder.build();
    }
}
