package uk.gov.hmcts.cmc.claimstore.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Component
public class JsonMappingHelper {

    @Autowired
    protected JsonMapper jsonMapper;

    public String toJson(Object input) {
        return jsonMapper.toJson(input);
    }

    public <T> T deserializeObjectFrom(MvcResult result, Class<T> targetClass) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), targetClass);
    }

    public List<Claim> deserializeListFrom(MvcResult result) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), new TypeReference<List<Claim>>() {
        });
    }
}
