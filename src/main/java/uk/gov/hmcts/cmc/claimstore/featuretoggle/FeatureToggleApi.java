package uk.gov.hmcts.cmc.claimstore.featuretoggle;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "feature-toggle-api", url = "${featureToggle.api.url}")
public interface FeatureToggleApi {

    @RequestMapping(method = RequestMethod.GET, value = "/api/ff4j/check/{feature}")
    boolean checkFeature(@PathVariable("feature") String feature);
}
