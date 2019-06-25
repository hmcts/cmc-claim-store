package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;

import java.util.Objects;

public class ExpertReportAssert extends AbstractAssert<ExpertReportAssert, ExpertReport> {

    public ExpertReportAssert(ExpertReport actual) {
        super(actual, ExpertReportAssert.class);
    }

    public ExpertReportAssert isEqualTo(CCDExpertReport ccdExpertReportRow) {
        isNotNull();

        if (!Objects.equals(actual.getExpertName(), ccdExpertReportRow.getExpertName())) {
            failWithMessage("Expected ExpertReport.expertName to be <%s> but was <%s>",
                ccdExpertReportRow.getExpertName(), actual.getExpertName());
        }

        if (!Objects.equals(actual.getExpertReportDate(), ccdExpertReportRow.getExpertReportDate())) {
            failWithMessage("Expected ExpertReport.expertReportDate to be <%s> but was <%s>",
                ccdExpertReportRow.getExpertReportDate(), actual.getExpertReportDate());
        }

        return this;
    }
}
