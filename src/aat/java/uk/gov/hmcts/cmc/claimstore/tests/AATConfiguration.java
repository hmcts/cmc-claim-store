package uk.gov.hmcts.cmc.claimstore.tests;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Component
@Validated
@ConfigurationProperties
public class AATConfiguration {

    @Valid
    @NotNull
    private TestUser smokeTestCitizen;

    @Valid
    @NotNull
    private TestUser smokeTestSolicitor;

    @NotBlank
    private String testInstanceUri;

    @NotBlank
    private String generatedUserEmailPattern;

    public TestUser getSmokeTestCitizen() {
        return smokeTestCitizen;
    }

    public void setSmokeTestCitizen(TestUser smokeTestCitizen) {
        this.smokeTestCitizen = smokeTestCitizen;
    }

    public TestUser getSmokeTestSolicitor() {
        return smokeTestSolicitor;
    }

    public void setSmokeTestSolicitor(TestUser smokeTestSolicitor) {
        this.smokeTestSolicitor = smokeTestSolicitor;
    }

    public String getTestInstanceUri() {
        return testInstanceUri;
    }

    public void setTestInstanceUri(String testInstanceUri) {
        this.testInstanceUri = testInstanceUri;
    }

    public String getGeneratedUserEmailPattern() {
        return generatedUserEmailPattern;
    }

    public void setGeneratedUserEmailPattern(String generatedUserEmailPattern) {
        this.generatedUserEmailPattern = generatedUserEmailPattern;
    }

}
