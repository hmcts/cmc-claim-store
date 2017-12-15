package uk.gov.hmcts.cmc.claimstore.documents.content.models;

public class StatementOfValueContent {

    private String personalInjury;
    private String housingDisrepair;
    private String claimValue;

    public StatementOfValueContent(String personalInjury, String housingDisrepair, String claimValue) {
        this.personalInjury = personalInjury;
        this.housingDisrepair = housingDisrepair;
        this.claimValue = claimValue;
    }

    public String getClaimValue() {
        return claimValue;
    }

    public String getPersonalInjury() {
        return personalInjury;
    }

    public String getHousingDisrepair() {
        return housingDisrepair;
    }

}
