package uk.gov.hmcts.cmc.domain.models;

public enum ScannedDocumentSubtype {

    OCON9X("OCON9x");

    public final String value;

    ScannedDocumentSubtype(String value) {
        this.value = value;
    }
}
