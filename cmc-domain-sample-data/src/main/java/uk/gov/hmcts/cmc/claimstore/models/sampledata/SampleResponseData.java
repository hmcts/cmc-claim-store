package uk.gov.hmcts.cmc.claimstore.models.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;

public class SampleResponseData {

    private ResponseData.ResponseType responseType = ResponseData.ResponseType.OWE_ALL_PAID_ALL;
    private ResponseData.FreeMediationOption freeMediationOption = ResponseData.FreeMediationOption.YES;
    private ResponseData.MoreTimeNeededOption moreTimeNeededOption = ResponseData.MoreTimeNeededOption.YES;
    private String defence = "defence string";
    private Party defendantDetails = SampleParty.builder().withRepresentative(null).individual();
    private StatementOfTruth statementOfTruth;

    public static SampleResponseData builder() {
        return new SampleResponseData();
    }

    public static ResponseData validDefaults() {
        return builder().build();
    }

    public SampleResponseData withResponseType(final ResponseData.ResponseType responseType) {
        this.responseType = responseType;
        return this;
    }

    public SampleResponseData withMediation(final ResponseData.FreeMediationOption freeMediationOption) {
        this.freeMediationOption = freeMediationOption;
        return this;
    }

    public SampleResponseData withDefence(final String defence) {
        this.defence = defence;
        return this;
    }

    public SampleResponseData withDefendantDetails(final Party sampleDefendantDetails) {
        this.defendantDetails = sampleDefendantDetails;
        return this;
    }

    public ResponseData build() {
        return new ResponseData(
            responseType, defence, freeMediationOption, moreTimeNeededOption, defendantDetails, statementOfTruth
        );
    }

    public SampleResponseData withStatementOfTruth(final String signerName, final String signerRole) {
        this.statementOfTruth = new StatementOfTruth(signerName,signerRole);
        return this;
    }
}
