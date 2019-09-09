package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOtherDirectionHeaderType;

import java.time.LocalDate;
import java.util.List;

@Builder
@Value
@JsonTypeName(value = "OtherDirection")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtherDirection {
    private CCDOrderDirectionType extraOrderDirection;

    private CCDOtherDirectionHeaderType otherDirectionHeaders;

    private String directionComment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate sendBy;

    private CCDDirectionPartyType forParty;

    private List<CCDCollectionElement<String>> extraDocUploadList;

    private List<CCDCollectionElement<String>> expertReports;
}
