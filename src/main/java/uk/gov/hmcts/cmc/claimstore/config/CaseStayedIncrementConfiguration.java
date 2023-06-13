package uk.gov.hmcts.cmc.claimstore.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@PropertySource("classpath:case-stayed/page-increments.properties")
public class CaseStayedIncrementConfiguration {

    @Value("${page.number}")
    private Integer pageIncrement;
}
