package uk.gov.hmcts.cmc.claimstore.config.properties.notifications;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Component
@Validated
@Getter
@Setter
public class EmailTemplates {

    @NotEmpty
    private String claimantClaimIssued;

    @NotEmpty
    private String defendantClaimIssued;

    @NotEmpty
    private String defendantResponseIssuedToIndividual;

    @NotEmpty
    private String defendantResponseNeeded;

    @NotEmpty
    private String defendantResponseIssuedToCompany;

    @NotEmpty
    private String claimantResponseIssued;

    @NotEmpty
    private String claimantResponseWithMediationIssued;

    @NotEmpty
    private String claimantResponseWithNoMediationIssued;

    @NotEmpty
    private String defendantResponseWithNoMediationIssued;

    @NotEmpty
    private String staffMoreTimeRequested;

    @NotEmpty
    private String defendantMoreTimeRequested;

    @NotEmpty
    private String claimantMoreTimeRequested;

    @NotEmpty
    private String representativeClaimIssued;

    @NotEmpty
    private String claimantCCJRequested;

    @NotEmpty
    private String defendantOfferMade;

    @NotEmpty
    private String claimantOfferMade;

    @NotEmpty
    private String offerAcceptedByClaimantEmailToClaimant;

    @NotEmpty
    private String offerAcceptedByClaimantEmailToDefendant;

    @NotEmpty
    private String offerRejectedByClaimantEmailToClaimant;

    @NotEmpty
    private String offerRejectedByClaimantEmailToDefendant;

    @NotEmpty
    private String offerCounterSignedEmailToOriginator;

    @NotEmpty
    private String offerCounterSignedEmailToOtherParty;

    @NotEmpty
    private String responseByClaimantEmailToDefendant;

    @NotEmpty
    private String claimantSaysDefendantHasPaidInFull;

    @NotEmpty
    private String redeterminationEmailToClaimant;
}
