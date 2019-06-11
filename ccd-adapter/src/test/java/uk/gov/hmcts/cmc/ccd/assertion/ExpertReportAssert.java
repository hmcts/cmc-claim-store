package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReportRow;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;

import java.util.Objects;

public class ExpertReportAssert extends AbstractAssert<ExpertReportAssert, ExpertReport> {

    public ExpertReportAssert(ExpertReport actual) {
        super(actual, ExpertReportAssert.class);
    }

    public ExpertReportAssert isEqualTo(CCDExpertReportRow ccdExpertReportRow) {
        isNotNull();

        if (!Objects.equals(actual.getExpertName(), ccdExpertReportRow.getExpertName())) {
            failWithMessage("Expected Expert Report Row to be <%s> but was <%s>",
                ccdExpertReportRow.getExpertName(), actual.getExpertName());
        }

        if (!Objects.equals(actual.getExpertReportDate(), ccdExpertReportRow.getExpertReportDate())) {
            failWithMessage("Expected Expert Report Row to be <%s> but was <%s>",
                ccdExpertReportRow.getExpertReportDate(), actual.getExpertReportDate());
        }

        return this;
    }
}
