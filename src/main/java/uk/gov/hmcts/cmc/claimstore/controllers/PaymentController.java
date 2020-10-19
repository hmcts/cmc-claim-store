package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.PaymentUpdate;
import uk.gov.hmcts.cmc.domain.models.paymentresponse.UpdatePaymentResponse;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Api
@RestController
@RequestMapping(
    path = "/payment",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {

    private final ClaimService claimService;

    private final AuthTokenValidator authTokenValidator;

    public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    @Autowired
    public PaymentController(ClaimService claimService, AuthTokenValidator authTokenValidator) {
        this.claimService = claimService;
        this.authTokenValidator = authTokenValidator;
    }

    @PostMapping(value = "/update-card-payment", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Update a Card payment")
    public ResponseEntity<UpdatePaymentResponse> updateCardPayment(
        @RequestHeader(value = SERVICE_AUTHORIZATION_HEADER) String serviceToken,
        @Valid @NotNull @RequestBody PaymentUpdate paymentUpdate
    ) {
        String serviceName = authTokenValidator.getServiceName(serviceToken);
        if (!"fees_and_payments".contains(serviceName)) {
            throw new BadRequestException("Invalid Service Token");
        }
        claimService.updateCardPayment(serviceToken, paymentUpdate);
        return ResponseEntity.ok().build();
    }
}
