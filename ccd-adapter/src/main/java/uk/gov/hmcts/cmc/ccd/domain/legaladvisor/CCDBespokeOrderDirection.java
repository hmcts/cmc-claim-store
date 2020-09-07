package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CCDBespokeOrderDirection {

    private CCDDirectionPartyType beSpokeDirectionFor;

    private String beSpokeDirectionExplain;

    private LocalDate beSpokeDirectionDatetime;

}
