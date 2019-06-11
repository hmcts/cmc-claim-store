package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
@JsonTypeName(value = "OtherDirection")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CCDOrderDirection {

    private CCDOrderDirectionType extraOrderDirection;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)

    private String directionComment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate sendBy;

    private CCDDirectionPartyType forParty;

    private List<CCDCollectionElement<String>> extraDocUploadList;

    private List<CCDCollectionElement<String>> expertReports;

    @JsonProperty("expertReports")
    void setExpertReports(List<CCDCollectionElement<String>> expertReports) {
        setExpertReports(expertReports);
    }

    @JsonProperty("expertReportPermissionStatementList")
    List<CCDCollectionElement<String>> getExpertReports() {
        return expertReports;
    }
}
