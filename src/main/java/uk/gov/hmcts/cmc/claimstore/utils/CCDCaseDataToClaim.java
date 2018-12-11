package uk.gov.hmcts.cmc.claimstore.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

@Component
public class CCDCaseDataToClaim {

    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;

    public CCDCaseDataToClaim(CaseMapper caseMapper, JsonMapper jsonMapper) {

        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
    }

    public Claim to(Long caseId, Map<String, Object> data) {
        Map<String, Object> tempData = new HashMap<>(data);
        tempData.put("id", caseId);

        CCDCase ccdCase = convertToCCDCase(tempData);
        return caseMapper.from(ccdCase);
    }

    private CCDCase convertToCCDCase(Map<String, Object> mapData) {
        return jsonMapper.fromMap(mapData, CCDCase.class);
    }
}
