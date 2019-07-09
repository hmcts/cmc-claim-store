package uk.gov.hmcts.cmc.claimstore.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd_adapter.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

@Component
public class CaseDetailsConverter {

    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;

    public CaseDetailsConverter(CaseMapper caseMapper, JsonMapper jsonMapper) {

        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
    }

    public Claim extractClaim(CaseDetails caseDetails) {
        return caseMapper.from(extractCCDCase(caseDetails));
    }

    public CCDCase extractCCDCase(CaseDetails caseDetails) {
        Map<String, Object> tempData = new HashMap<>(caseDetails.getData());
        tempData.put("id", caseDetails.getId());
        tempData.put("state", caseDetails.getState());

        return extractCCDCase(tempData);
    }

    private CCDCase extractCCDCase(Map<String, Object> mapData) {
        return jsonMapper.fromMap(mapData, CCDCase.class);
    }
}
