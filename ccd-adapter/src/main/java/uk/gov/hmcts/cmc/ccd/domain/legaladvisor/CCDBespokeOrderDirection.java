package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class CCDBespokeOrderDirection {

    private CCDDirectionPartyType beSpokeDirectionFor;

    private String beSpokeDirectionExplain;

    private LocalDate beSpokeDirectionDatetime;

}
