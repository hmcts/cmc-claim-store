package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class CCDOcon9xMain {
    private CCDLableCode selectedDocument;
    private List<CCDLableCode> reviewedDocuments;
}
