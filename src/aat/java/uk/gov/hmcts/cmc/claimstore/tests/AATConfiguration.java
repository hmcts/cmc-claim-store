package uk.gov.hmcts.cmc.claimstore.tests;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "aat")
public class AATConfiguration {

    @Valid
    @NotNull
    private TestUser testCitizenUser;

    @Valid
    @NotNull
    private TestUser testSolicitorUser;

    @NotBlank
    private String testInstanceUri;

    @NotBlank
    private String testUserEmailPattern;

    public TestUser getTestCitizenUser() {
        return testCitizenUser;
    }

    public void setTestCitizenUser(TestUser testCitizenUser) {
        this.testCitizenUser = testCitizenUser;
    }

    public TestUser getTestSolicitorUser() {
        return testSolicitorUser;
    }

    public void setTestSolicitorUser(TestUser testSolicitorUser) {
        this.testSolicitorUser = testSolicitorUser;
    }

    public String getTestInstanceUri() {
        return testInstanceUri;
    }

    public void setTestInstanceUri(String testInstanceUri) {
        this.testInstanceUri = testInstanceUri;
    }

    public String getTestUserEmailPattern() {
        return testUserEmailPattern;
    }

    public void setTestUserEmailPattern(String testUserEmailPattern) {
        this.testUserEmailPattern = testUserEmailPattern;
    }

}
