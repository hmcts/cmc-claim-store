package uk.gov.hmcts.cmc.ccd.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseDetails {
    private Long id;
    private String jurisdiction;
    @JsonProperty("case_type_id")
    private String caseTypeId;
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    @JsonProperty("last_modified")
    private LocalDateTime lastModified;
    private String state;
    @JsonProperty("locked_by_user_id")
    private Integer lockedBy;
    @JsonProperty("security_level")
    private Integer securityLevel;
    @JsonProperty("case_data")
    private Map<String, JsonNode> data;
    @JsonProperty("security_classification")
    private Classification securityClassification;
}
