package uk.gov.hmcts.cmc.ccd.assertion.response;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponseType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {

    public ResponseAssert(Response response) {
        super(response, ResponseAssert.class);
    }

    public ResponseAssert isEqualTo(CCDResponse ccdResponse) {
        isNotNull();

        if (actual instanceof FullDefenceResponse) {
            if (!Objects.equals(CCDResponseType.FULL_DEFENCE, ccdResponse.getResponseType())) {
                failWithMessage("Expected CCDResponse.type to be <%s> but was <%s>",
                    ccdResponse.getResponseType(), CCDResponseType.FULL_DEFENCE);
            }
            assertThat((FullDefenceResponse) actual).isEqualTo(ccdResponse.getFullDefenceResponse());
        }

        return this;
    }
}
