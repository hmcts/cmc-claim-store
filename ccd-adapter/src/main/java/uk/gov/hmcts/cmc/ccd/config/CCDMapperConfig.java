package uk.gov.hmcts.cmc.ccd.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.cmc.ccd.mapper"})
public class CCDMapperConfig {
}
