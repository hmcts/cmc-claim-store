package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.TemplateConstants.HMCTS_URL;

@Builder
@Value
public class OtherDirection {
    private CCDOrderDirectionType extraOrderDirection;
    private String directionComment;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate sendBy;
    private CCDDirectionPartyType forParty;
    private final String hmctsURL = HMCTS_URL;
}
