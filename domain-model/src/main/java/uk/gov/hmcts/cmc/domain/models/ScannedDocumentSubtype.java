package uk.gov.hmcts.cmc.domain.models;

public enum ScannedDocumentSubtype {

    /* Note: This is not an exhaustive list of the subtypes used in CCD */
    OCON9X("OCON9x");

    public final String value;

    ScannedDocumentSubtype(String value) {
        this.value = value;
    }
}
