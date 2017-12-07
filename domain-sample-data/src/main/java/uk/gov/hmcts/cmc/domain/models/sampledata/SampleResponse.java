package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;

public class SampleResponse {

    private Response.ResponseType responseType = Response.ResponseType.OWE_NONE;
    private Response.FreeMediationOption freeMediationOption = Response.FreeMediationOption.YES;
    private Response.MoreTimeNeededOption moreTimeNeededOption = Response.MoreTimeNeededOption.YES;
    private String defence = "defence string";
    private Party defendantDetails = SampleParty.builder().withRepresentative(null).individual();
    private StatementOfTruth statementOfTruth;

    public static SampleResponse builder() {
        return new SampleResponse();
    }

    public static Response validDefaults() {
        return builder().build();
    }

    public SampleResponse withResponseType(final Response.ResponseType responseType) {
        this.responseType = responseType;
        return this;
    }

    public SampleResponse withMediation(final Response.FreeMediationOption freeMediationOption) {
        this.freeMediationOption = freeMediationOption;
        return this;
    }

    public SampleResponse withDefence(final String defence) {
        this.defence = defence;
        return this;
    }

    public SampleResponse withDefendantDetails(final Party sampleDefendantDetails) {
        this.defendantDetails = sampleDefendantDetails;
        return this;
    }

    public Response build() {
        return new Response(
            responseType, defence, freeMediationOption, moreTimeNeededOption, defendantDetails, statementOfTruth
        );
    }

    public SampleResponse withStatementOfTruth(final String signerName, final String signerRole) {
        this.statementOfTruth = new StatementOfTruth(signerName,signerRole);
        return this;
    }
}
