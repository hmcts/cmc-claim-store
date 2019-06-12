package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;

@Component
public class ExpertRowMapper {

    public CCDCollectionElement<CCDExpertReport> to(ExpertReport expertReportRow) {

        if (expertReportRow == null) {
            return null;
        }

        CCDExpertReport.CCDExpertReportBuilder builder = CCDExpertReport.builder();

        return CCDCollectionElement.<CCDExpertReport>builder()
            .value(builder.expertName(expertReportRow.getExpertName())
                    .expertReportDate(expertReportRow.getExpertReportDate())
                    .build())
            .id(expertReportRow.getId())
            .build();
    }

    public ExpertReport from(CCDCollectionElement<CCDExpertReport> collectionElement) {
        CCDExpertReport ccdExpertReportRow = collectionElement.getValue();

        if (ccdExpertReportRow == null) {
            return null;
        }

        return ExpertReport
            .builder()
            .id(ccdExpertReportRow.getId())
            .expertName(ccdExpertReportRow.getExpertName())
            .expertReportDate(ccdExpertReportRow.getExpertReportDate())
            .build();
    }
}
