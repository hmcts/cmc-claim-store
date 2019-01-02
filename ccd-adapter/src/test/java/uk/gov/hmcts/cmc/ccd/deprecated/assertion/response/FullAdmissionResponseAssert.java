package uk.gov.hmcts.cmc.ccd.deprecated.assertion.response;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDFullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class FullAdmissionResponseAssert extends AbstractAssert<FullAdmissionResponseAssert, FullAdmissionResponse> {

    public FullAdmissionResponseAssert(FullAdmissionResponse actual) {
        super(actual, FullAdmissionResponseAssert.class);
    }

    public FullAdmissionResponseAssert isEqualTo(CCDFullAdmissionResponse ccdFullAdmissionResponse) {
        isNotNull();

        if (!Objects.equals(actual.getMoreTimeNeeded().name(),
            ccdFullAdmissionResponse.getMoreTimeNeededOption().name())
        ) {
            failWithMessage("Expected FullAdmissionResponse.moreTimeNeeded to be <%s> but was <%s>",
                ccdFullAdmissionResponse.getMoreTimeNeededOption(), actual.getMoreTimeNeeded().name());
        }

        actual.getFreeMediation().ifPresent(freeMediation -> {
            if (!Objects.equals(freeMediation.name(), ccdFullAdmissionResponse.getFreeMediationOption().name())) {
                failWithMessage("Expected FullAdmissionResponse.freeMediation to be <%s> but was <%s>",
                    ccdFullAdmissionResponse.getFreeMediationOption(), freeMediation);
            }
        });

        //assertThat(actual.getDefendant()).isEqualTo(ccdFullAdmissionResponse.getDefendant());

        //assertThat(actual.getPaymentIntention()).isEqualTo(ccdFullAdmissionResponse.getPaymentIntention());

        actual.getStatementOfMeans().ifPresent(statementOfMeans ->
            assertThat(statementOfMeans).isEqualTo(ccdFullAdmissionResponse.getStatementOfMeans())
        );
        actual.getStatementOfTruth().ifPresent(statementOfTruth ->
            assertThat(statementOfTruth).isEqualTo(ccdFullAdmissionResponse.getStatementOfTruth())
        );

        return this;
    }
}
