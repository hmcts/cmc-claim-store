package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;

public abstract class SampleResponse<T extends SampleResponse<T>> {

    public static class FullDefence extends SampleResponse<FullDefence> {
        private FullDefenceResponse.DefenceType defenceType = FullDefenceResponse.DefenceType.DISPUTE;
        private String defence = "defence string";
        private PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder().build();
        private DefendantTimeline timeline = SampleDefendantTimeline.validDefaults();

        public static FullDefence builder() {
            return new FullDefence();
        }

        public FullDefence withDefenceType(FullDefenceResponse.DefenceType defenceType) {
            this.defenceType = defenceType;
            return this;
        }

        public FullDefence withDefence(String defence) {
            this.defence = defence;
            return this;
        }

        public FullDefence withPaymentDeclaration(PaymentDeclaration paymentDeclaration) {
            this.paymentDeclaration = paymentDeclaration;
            return this;
        }

        public FullDefence withTimeline(DefendantTimeline timeline) {
            this.timeline = timeline;
            return this;
        }

        public FullDefenceResponse build() {
            return new FullDefenceResponse(
                freeMediationOption, moreTimeNeededOption, defendantDetails, statementOfTruth,
                defenceType, defence, paymentDeclaration, timeline
            );
        }
    }

    protected Response.FreeMediationOption freeMediationOption = Response.FreeMediationOption.YES;
    protected Response.MoreTimeNeededOption moreTimeNeededOption = Response.MoreTimeNeededOption.YES;
    protected Party defendantDetails = SampleParty.builder().withRepresentative(null).individual();
    protected StatementOfTruth statementOfTruth;

    public static FullDefenceResponse validDefaults() {
        return FullDefence.builder().build();
    }

    public SampleResponse<T> withMediation(Response.FreeMediationOption freeMediationOption) {
        this.freeMediationOption = freeMediationOption;
        return this;
    }

    public SampleResponse<T> withMoreTimeNeededOption(Response.MoreTimeNeededOption moreTimeNeededOption) {
        this.moreTimeNeededOption = moreTimeNeededOption;
        return this;
    }

    public SampleResponse<T> withDefendantDetails(Party sampleDefendantDetails) {
        this.defendantDetails = sampleDefendantDetails;
        return this;
    }

    public SampleResponse<T> withStatementOfTruth(String signerName, String signerRole) {
        this.statementOfTruth = new StatementOfTruth(signerName, signerRole);
        return this;
    }

    public abstract Response build();
}
