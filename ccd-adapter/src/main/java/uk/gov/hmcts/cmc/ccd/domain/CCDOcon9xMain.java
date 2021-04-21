package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@SuppressWarnings("MemberName")
public class CCDOcon9xMain {
    private CCDLableCode value;
    //Below field is not needed at the moment
    private List<CCDLableCode> list_items;
}
