package uk.gov.hmcts.cmc.domain.statementofmeans;

public enum ResidenceType {
    OWN_HOME("Home you own yourself (or pay a mortgage on)"),
    JOINT_OWN_HOME("Jointly-owned home (or jointly mortgaged home)"),
    PRIVATE_RENTAL("Private rental"),
    COUNCIL_OR_HOUSING_ASSN_HOME("Council or housing association home"),
    OTHER("Other");

    String description;

    ResidenceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
