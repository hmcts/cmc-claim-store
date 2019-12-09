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
    private String defendantResponseIssued;

    @NotEmpty
    private String defendantResponseNeeded;

    @NotEmpty
    private String claimantResponseIssued;

    @NotEmpty
    private String claimantResponseWithMediationIssued;

    @NotEmpty
    private String claimantResponseWithNoMediationIssued;

    @NotEmpty
    private String claimantResponseForDqPilotWithNoMediationIssued;

    @NotEmpty
    private String defendantResponseWithNoMediationIssued;

    @NotEmpty
    private String defendantResponseForDqPilotWithNoMediationIssued;

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
    private String claimantRequestedInterlocutoryJudgement;

    @NotEmpty
    private String redeterminationEmailToClaimant;

    @NotEmpty
    private String settlementRejectedEmailToClaimant;

    @NotEmpty
    private String settlementRejectedEmailToDefendant;

    @NotEmpty
    private String claimantSignedSettlementAgreementToDefendant;

    @NotEmpty
    private String claimantSignedSettlementAgreementToClaimant;

    @NotEmpty
    private String defendantSignedSettlementAgreementToDefendant;

    @NotEmpty
    private String defendantSignedSettlementAgreementToClaimant;

    @NotEmpty
    private String defendantAdmissionResponseToClaimant;

    @NotEmpty
    private String claimantRejectedPartAdmitOrStatesPaidEmailToDefendant;

    @NotEmpty
    private String claimantLegalOrderDrawn;

    @NotEmpty
    private String defendantLegalOrderDrawn;

    @NotEmpty
    private String defendantFreeMediationConfirmation;

    @NotEmpty
    private String reviewOrderEmailToClaimant;

    @NotEmpty
    private String reviewOrderEmailToDefendant;

    @NotEmpty
    private String claimantIntentionToProceedForPaperDq;

    @NotEmpty
    private String claimantIntentionToProceedForOnlineDq;

    @NotEmpty
    private  String claimantSettledAfterFullDefence;

    @NotEmpty
    private  String claimantMediationSuccess;

    @NotEmpty
    private  String defendantMediationSuccess;

    @NotEmpty
    private String claimantReadyForTransfer;

    @NotEmpty
    private String defendantReadyForTransfer;

    @NotEmpty
    private String claimantMediationFailureOfflineDQ;

    @NotEmpty
    private String defendantMediationFailureOfflineDQ;

}
