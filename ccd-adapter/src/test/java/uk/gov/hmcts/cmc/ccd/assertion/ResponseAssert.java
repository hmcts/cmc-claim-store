package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ResponseAssert extends AbstractAssert<ResponseAssert, FullDefenceResponse> {

    public ResponseAssert(FullDefenceResponse actual) {
        super(actual, ResponseAssert.class);
    }

    public ResponseAssert isEqualTo(CCDResponse ccdResponse) {
        isNotNull();

        actual.getFreeMediation().ifPresent(freeMediation -> {
            if (!Objects.equals(freeMediation.name(), ccdResponse.getFreeMediation().name())) {
                failWithMessage("Expected FullDefenceResponse.freeMediation to be <%s> but was <%s>",
                    ccdResponse.getFreeMediation(), freeMediation);
            }
        });

        if (!Objects.equals(actual.getMoreTimeNeeded().name(), ccdResponse.getMoreTimeNeeded().name())) {
            failWithMessage("Expected FullDefenceResponse.moreTimeNeeded to be <%s> but was <%s>",
                ccdResponse.getMoreTimeNeeded(), actual.getMoreTimeNeeded().name());
        }

        if (!Objects.equals(actual.getDefence(), ccdResponse.getDefence())) {
            failWithMessage("Expected FullDefenceResponse.defence to be <%s> but was <%s>",
                ccdResponse.getDefence(), actual.getDefence());
        }

        if (!Objects.equals(actual.getDefenceType().name(), ccdResponse.getDefenceType().name())) {
            failWithMessage("Expected FullDefenceResponse.defenceType to be <%s> but was <%s>",
                ccdResponse.getDefenceType().name(), actual.getDefenceType().name());
        }

        assertThat(actual.getDefendant()).isEqualTo(ccdResponse.getDefendant());

        actual.getStatementOfTruth().ifPresent(statementOfTruth ->
            assertThat(statementOfTruth).isEqualTo(ccdResponse.getStatementOfTruth()));

        return this;
    }
}
