package uk.gov.hmcts.cmc.claimstore.tests.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cmc.claimstore.tests.exception.ForbiddenException;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder delegate = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 403) {
            // we want to handle and ignore this exception
            // IDAM returns when creating with users that already exist
            // this could be the case with retry logic - so ignore and just authenticate
            return new ForbiddenException("Already Exists");
        } else if (response.status() == 404) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested resource not found");
        }
        return delegate.decode(methodKey, response);
    }
}
