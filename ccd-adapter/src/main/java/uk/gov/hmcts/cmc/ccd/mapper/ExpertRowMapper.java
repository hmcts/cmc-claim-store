package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReportRow;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;

@Component
public class ExpertRowMapper {

    public CCDCollectionElement<CCDExpertReportRow> to(ExpertReport expertReportRow) {

        if (expertReportRow == null) {
            return null;
        }

        CCDExpertReportRow.CCDExpertReportRowBuilder builder = CCDExpertReportRow.builder();

        return CCDCollectionElement.<CCDExpertReportRow>builder()
            .value(builder.expertName(expertReportRow.getExpertName())
                    .expertReportDate(expertReportRow.getExpertReportDate())
                    .build())
            .id(expertReportRow.getId())
            .build();
    }

    public ExpertReport from(CCDCollectionElement<CCDExpertReportRow> collectionElement) {
        CCDExpertReportRow ccdExpertReportRow = collectionElement.getValue();

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
