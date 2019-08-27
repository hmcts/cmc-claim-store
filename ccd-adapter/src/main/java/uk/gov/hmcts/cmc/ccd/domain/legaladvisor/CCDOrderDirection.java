package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class CCDOrderDirection {

    private CCDOrderDirectionType extraOrderDirection;

    private CCDOtherDirectionHeaderType otherDirectionHeaders;

    private String directionComment;

    private LocalDate sendBy;

    private CCDDirectionPartyType forParty;

    private List<CCDCollectionElement<String>> extraDocUploadList;

    private List<CCDCollectionElement<String>> expertReports;
}
