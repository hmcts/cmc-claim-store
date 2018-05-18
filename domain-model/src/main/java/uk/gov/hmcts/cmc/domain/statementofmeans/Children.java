package uk.gov.hmcts.cmc.domain.statementofmeans;

public class Children {

    private final int under11;
    private final int between11and15;
    private final int between16and19;

    public Children(int under11, int between11and15, int between16and19) {
        this.under11 = under11;
        this.between11and15 = between11and15;
        this.between16and19 = between16and19;
    }
}
