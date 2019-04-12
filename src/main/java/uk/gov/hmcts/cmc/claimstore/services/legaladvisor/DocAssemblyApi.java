package uk.gov.hmcts.cmc.claimstore.services.legaladvisor;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "doc-assembly-api", url = "${docAssembly.api.url}")
public interface DocAssemblyApi {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/api/template-renditions",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    DocAssemblyResponse generateOrder(
        DocAssemblyRequest requestBody,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader("ServiceAuthorisation") String serviceAuthorisation
    );
}
