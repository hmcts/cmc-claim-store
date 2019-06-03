package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReportRow;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReportRow;

@Component
public class ExpertRowMapper {

    public CCDCollectionElement<CCDExpertReportRow> to(ExpertReportRow expertReportRow) {

        CCDExpertReportRow.CCDExpertReportRowBuilder builder = CCDExpertReportRow.builder();

        return CCDCollectionElement.<CCDExpertReportRow>builder()
            .value(builder.expertName(expertReportRow.getExpertName())
                    .expertReportDate(expertReportRow.getExpertReportDate())
                    .build())
            .id(expertReportRow.getId())
            .build();
    }

    public ExpertReportRow from(CCDCollectionElement<CCDExpertReportRow> ccdExpertReportRow) {

        return ExpertReportRow
            .builder()
            .id(ccdExpertReportRow.getId())
            .expertName(ccdExpertReportRow.getValue().getExpertName())
            .expertReportDate(ccdExpertReportRow.getValue().getExpertReportDate())
            .build();
    }
}
