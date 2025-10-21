package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;

import java.util.Optional;

public class ExpertReportAssert extends CustomAssert<ExpertReportAssert, ExpertReport> {

    ExpertReportAssert(ExpertReport actual) {
        super("ExpertReport", actual, ExpertReportAssert.class);
    }

    public ExpertReportAssert isEqualTo(CCDExpertReport expected) {
        isNotNull();

        compare("expertName",
            expected.getExpertName(),
            Optional.ofNullable(actual.getExpertName()));

        compare("expertReportDate",
            expected.getExpertReportDate(),
            Optional.ofNullable(actual.getExpertReportDate()));

        return this;
    }
}
