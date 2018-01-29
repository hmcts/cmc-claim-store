package uk.gov.hmcts.cmc.claimstore.config;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class AppInsightsConfig {

    @Bean
    public String telemetryConfig() {
        String telemetryKey = "f8c7905e-861c-4e9a-83f8-a63099a73f6e";
        //String telemetryKey = System.getenv("APPLICATION_INSIGHTS_IKEY");
        //if (telemetryKey != null) {
        TelemetryConfiguration.getActive().setInstrumentationKey(telemetryKey);
        //}
        return telemetryKey;
    }

    @Bean
    public FilterRegistrationBean aiFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new WebRequestTrackingFilter());
        registration.addUrlPatterns("/**");
        registration.setOrder(1);
        return registration;
    }

    @Bean(name = "WebRequestTrackingFilter")
    public Filter webRequestTrackingFilter() {
        return new WebRequestTrackingFilter();
    }

}
