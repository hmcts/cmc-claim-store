package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum CCDOrderDirectionType {
    DOCUMENTS,
    EYEWITNESS,
    OTHER,
    EXPERT_REPORT_PERMISSION
}
