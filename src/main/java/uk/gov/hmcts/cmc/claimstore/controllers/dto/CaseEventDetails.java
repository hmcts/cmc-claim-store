package uk.gov.hmcts.cmc.claimstore.controllers.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;

import java.util.List;

public class CaseEventDetails {
    @JsonProperty("caseEventDetailList")
    private List<CaseEventDetail> caseEventDetailList;

    @Builder
    @JsonCreator
    public CaseEventDetails(List<CaseEventDetail> caseEventDetailList) {
        this.caseEventDetailList = caseEventDetailList;
    }

    public List<CaseEventDetail> getCaseEventDetailList() {
        return caseEventDetailList;
    }

    public void setCaseEventDetailList(List<CaseEventDetail> caseEventDetailList) {
        this.caseEventDetailList = caseEventDetailList;
    }
}
