package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

public abstract class SampleResponse<T extends SampleResponse<T>> {

    public static PartAdmissionResponse validPartAdmissionDefaults() {
        return SamplePartAdmissionResponse.builder().build();
    }

    protected Response.FreeMediationOption freeMediationOption = Response.FreeMediationOption.YES;
    protected Response.MoreTimeNeededOption moreTimeNeededOption = Response.MoreTimeNeededOption.YES;
    protected Party defendantDetails = SampleParty.builder().withRepresentative(null).individual();
    protected StatementOfTruth statementOfTruth;

    public static Response validDefaults() {
        return SampleFullDefenceResponse.validDefaults();
    }

    public T withMediation(Response.FreeMediationOption freeMediationOption) {
        this.freeMediationOption = freeMediationOption;
        return (T)this;
    }

    public T withDefendantDetails(Party sampleDefendantDetails) {
        this.defendantDetails = sampleDefendantDetails;
        return (T)this;
    }

    public T withStatementOfTruth(String signerName, String signerRole) {
        this.statementOfTruth = new StatementOfTruth(signerName,signerRole);
        return (T)this;
    }

    public abstract Response build();
}
