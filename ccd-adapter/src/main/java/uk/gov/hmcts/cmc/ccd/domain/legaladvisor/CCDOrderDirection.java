package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CCDOrderDirection {
    private CCDOrderDirectionType extraOrderDirection;
    private String otherDirection;
    private LocalDate sendBy;
    private CCDDirectionPartyType forParty;
}
