package uk.gov.hmcts.cmc.ccd.assertion.response;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDFullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

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

        assertThat(actual.getDefendant()).isEqualTo(ccdFullAdmissionResponse.getDefendant());

        if (!Objects.equals(actual.getPaymentOption().name(), ccdFullAdmissionResponse.getPaymentOption().name())) {
            failWithMessage("Expected FullAdmissionResponse.paymentOption to be <%s> but was <%s>",
                ccdFullAdmissionResponse.getPaymentOption().name(), actual.getPaymentOption().name());
        }


        actual.getRepaymentPlan().ifPresent(
            repaymentPlan -> assertThat(repaymentPlan).isEqualTo(ccdFullAdmissionResponse.getRepaymentPlan())
        );

        actual.getPaymentDate().ifPresent(paymentDate -> {
            if (!Objects.equals(paymentDate, ccdFullAdmissionResponse.getPaymentDate())) {
                failWithMessage("Expected FullAdmissionResponse.paymentDate to be <%s> but was <%s>",
                    ccdFullAdmissionResponse.getPaymentDate(), paymentDate);
            }
        });

        actual.getStatementOfMeans().ifPresent(statementOfMeans ->
            assertThat(statementOfMeans).isEqualTo(ccdFullAdmissionResponse.getStatementOfMeans())
        );
        actual.getStatementOfTruth().ifPresent(statementOfTruth ->
            assertThat(statementOfTruth).isEqualTo(ccdFullAdmissionResponse.getStatementOfTruth())
        );

        return this;
    }
}
