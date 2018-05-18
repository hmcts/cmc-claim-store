package uk.gov.hmcts.cmc.domain.statementofmeans;

public class Employer {

    private final String jobTitle;
    private final String employerName;

    public Employer(String jobTitle, String employerName) {
        this.jobTitle = jobTitle;
        this.employerName = employerName;
    }
}
