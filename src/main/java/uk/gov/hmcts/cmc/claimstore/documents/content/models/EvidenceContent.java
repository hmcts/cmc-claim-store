package uk.gov.hmcts.cmc.claimstore.documents.content.models;

public class EvidenceContent {

    private final String type;
    private final String description;

    public EvidenceContent(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
