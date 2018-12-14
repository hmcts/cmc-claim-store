package uk.gov.hmcts.cmc.ccd.deprecated.assertion.response;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class PartAdmissionResponseAssert extends AbstractAssert<PartAdmissionResponseAssert, PartAdmissionResponse> {

    public PartAdmissionResponseAssert(PartAdmissionResponse actual) {
        super(actual, PartAdmissionResponseAssert.class);
    }

    public PartAdmissionResponseAssert isEqualTo(CCDPartAdmissionResponse ccdPartAdmissionResponse) {
        isNotNull();

        if (!Objects.equals(actual.getMoreTimeNeeded().name(),
            ccdPartAdmissionResponse.getMoreTimeNeededOption().name())
        ) {
            failWithMessage("Expected PartAdmissionResponse.moreTimeNeeded to be <%s> but was <%s>",
                ccdPartAdmissionResponse.getMoreTimeNeededOption(), actual.getMoreTimeNeeded().name());
        }

        actual.getFreeMediation().ifPresent(freeMediation -> {
            if (!Objects.equals(freeMediation.name(), ccdPartAdmissionResponse.getFreeMediationOption().name())) {
                failWithMessage("Expected PartAdmissionResponse.freeMediation to be <%s> but was <%s>",
                    ccdPartAdmissionResponse.getFreeMediationOption(), freeMediation);
            }
        });

        assertThat(actual.getDefendant()).isEqualTo(ccdPartAdmissionResponse.getDefendant());
        Assertions.assertThat(actual.getDefence()).isEqualTo(ccdPartAdmissionResponse.getDefence());
        Assertions.assertThat(actual.getAmount()).isEqualTo(ccdPartAdmissionResponse.getAmount());

        /*actual.getPaymentIntention().ifPresent(paymentIntention ->
            assertThat(paymentIntention).isEqualTo(ccdPartAdmissionResponse.getPaymentIntention())
        );*/

        actual.getStatementOfMeans().ifPresent(statementOfMeans ->
            assertThat(statementOfMeans).isEqualTo(ccdPartAdmissionResponse.getStatementOfMeans())
        );

        actual.getStatementOfTruth().ifPresent(statementOfTruth ->
            assertThat(statementOfTruth).isEqualTo(ccdPartAdmissionResponse.getStatementOfTruth())
        );

        actual.getPaymentDeclaration().ifPresent(paymentDeclaration ->
            assertThat(paymentDeclaration).isEqualTo(ccdPartAdmissionResponse.getPaymentDeclaration())
        );

        actual.getTimeline().ifPresent(timeline ->
            assertThat(timeline).isEqualTo(ccdPartAdmissionResponse.getTimeline())
        );

        actual.getEvidence().ifPresent(evidence ->
            assertThat(evidence).isEqualTo(ccdPartAdmissionResponse.getEvidence())
        );

        return this;
    }
}
