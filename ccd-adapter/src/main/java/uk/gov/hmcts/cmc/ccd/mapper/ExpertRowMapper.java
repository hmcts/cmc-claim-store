package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;

@Component
public class ExpertRowMapper implements Mapper<CCDCollectionElement<CCDExpertReport>, ExpertReport> {

    @Override
    public CCDCollectionElement<CCDExpertReport> to(ExpertReport expertReport) {
        if (expertReport == null) {
            return null;
        }

        return CCDCollectionElement.<CCDExpertReport>builder()
            .value(CCDExpertReport.builder()
                .expertName(expertReport.getExpertName())
                .expertReportDate(expertReport.getExpertReportDate())
                .build())
            .id(expertReport.getId())
            .build();
    }

    @Override
    public ExpertReport from(CCDCollectionElement<CCDExpertReport> collectionElement) {
        CCDExpertReport expertReport = collectionElement.getValue();

        if (expertReport == null) {
            return null;
        }

        return ExpertReport
            .builder()
            .id(collectionElement.getId())
            .expertName(expertReport.getExpertName())
            .expertReportDate(expertReport.getExpertReportDate())
            .build();
    }
}
