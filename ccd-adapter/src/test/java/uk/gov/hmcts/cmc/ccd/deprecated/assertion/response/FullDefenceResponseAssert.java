package uk.gov.hmcts.cmc.ccd.deprecated.assertion.response;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDFullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class FullDefenceResponseAssert extends AbstractAssert<FullDefenceResponseAssert, FullDefenceResponse> {

    public FullDefenceResponseAssert(FullDefenceResponse actual) {
        super(actual, FullDefenceResponseAssert.class);
    }

    public FullDefenceResponseAssert isEqualTo(CCDFullDefenceResponse ccdFullDefenceResponse) {
        isNotNull();

        actual.getFreeMediation().ifPresent(freeMediation -> {
            if (!Objects.equals(freeMediation.name(), ccdFullDefenceResponse.getFreeMediationOption().name())) {
                failWithMessage("Expected FullDefenceResponse.freeMediation to be <%s> but was <%s>",
                    ccdFullDefenceResponse.getFreeMediationOption(), freeMediation);
            }
        });

        if (!Objects.equals(actual.getMoreTimeNeeded().name(),
            ccdFullDefenceResponse.getMoreTimeNeededOption().name())
            ) {
            failWithMessage("Expected FullDefenceResponse.moreTimeNeeded to be <%s> but was <%s>",
                ccdFullDefenceResponse.getMoreTimeNeededOption(), actual.getMoreTimeNeeded().name());
        }

        actual.getDefence().ifPresent(defence -> {
            if (!Objects.equals(defence, ccdFullDefenceResponse.getDefence())) {
                failWithMessage("Expected FullDefenceResponse.defence to be <%s> but was <%s>",
                    ccdFullDefenceResponse.getDefence(), defence);
            }
        });

        if (!Objects.equals(actual.getDefenceType().name(), ccdFullDefenceResponse.getDefenceType().name())) {
            failWithMessage("Expected defenceType to be <%s> but was <%s>",
                ccdFullDefenceResponse.getDefenceType().name(), actual.getDefenceType().name());
        }

        assertThat(actual.getDefendant()).isEqualTo(ccdFullDefenceResponse.getDefendant());

        actual.getStatementOfTruth().ifPresent(statementOfTruth ->
            assertThat(statementOfTruth).isEqualTo(ccdFullDefenceResponse.getStatementOfTruth()));

        return this;
    }
}
