package uk.gov.hmcts.cmc.domain.models;

public enum ScannedDocumentSubtype {

    /* Note: This is not an exhaustive list of the subtypes used in CCD */
    OCON9X("OCON9x"),
    N9("N9"),
    N9A("N9a"),
    N9B("N9b"),
    N11("N11");


    public final String value;

    ScannedDocumentSubtype(String value) {
        this.value = value;
    }
}
