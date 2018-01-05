package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;

public class SampleFullDefenceResponse extends SampleResponse<SampleFullDefenceResponse> {
    private FullDefenceResponse.DefenceType defenceType = FullDefenceResponse.DefenceType.DISPUTE;
    private String defence = "defence string";

    public static SampleFullDefenceResponse builder() {
        return new SampleFullDefenceResponse();
    }

    public static FullDefenceResponse validDefence() {
        return builder().build();
    }

    public SampleFullDefenceResponse withDefenceType(FullDefenceResponse.DefenceType defenceType) {
        this.defenceType = defenceType;
        return this;
    }

    public SampleFullDefenceResponse withDefence(String defence) {
        this.defence = defence;
        return this;
    }

    public FullDefenceResponse build() {
        return new FullDefenceResponse(
            freeMediationOption, moreTimeNeededOption, defendantDetails, statementOfTruth,
            defenceType, defence
        );
    }
}
