package uk.gov.hmcts.cmc.ccd_adapter.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.cmc.ccd_adapter.mapper"})
public class CCDMapperConfig {
}
