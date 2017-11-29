package uk.gov.hmcts.cmc.ccd.client.header;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class HttpHeadersFactory {

    private final String authorizationHeader;

    private final String serviceAuthorizationHeader;

    @Autowired
    public HttpHeadersFactory(
        @Value("${user.authorization}") String authorizationHeader,
        @Value("${service.authorization}") String serviceAuthorizationHeader) {
        this.authorizationHeader = authorizationHeader;
        this.serviceAuthorizationHeader = serviceAuthorizationHeader;
    }

    public HttpHeaders getHttpHeader() {
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = new MediaType(
            MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8")
        );
        headers.add("Authorization", authorizationHeader);
        headers.add("ServiceAuthorization", serviceAuthorizationHeader);
        headers.setContentType(contentType);
        return headers;
    }
}
