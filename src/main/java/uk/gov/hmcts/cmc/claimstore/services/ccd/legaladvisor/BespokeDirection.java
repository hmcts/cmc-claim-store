package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;

import java.time.LocalDate;

@Builder
@Value
@JsonTypeName(value = "BespokeDirection")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BespokeDirection {

    private CCDDirectionPartyType forParty;

    private String directionComment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate sendBy;

}
