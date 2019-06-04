package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReportRow;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReportRow;

import java.util.Objects;

public class ExpertReportRowAssert extends AbstractAssert<ExpertReportRowAssert, ExpertReportRow> {

    public ExpertReportRowAssert(ExpertReportRow actual) {
        super(actual, ExpertReportRowAssert.class);
    }

    public ExpertReportRowAssert isEqualTo(CCDExpertReportRow ccdExpertReportRow) {
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
