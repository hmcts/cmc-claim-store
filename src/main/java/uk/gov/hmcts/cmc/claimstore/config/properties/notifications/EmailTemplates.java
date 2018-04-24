package uk.gov.hmcts.cmc.claimstore.config.properties.notifications;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Component
@Validated
public class EmailTemplates {

    @NotEmpty
    private String claimantClaimIssued;

    @NotEmpty
    private String defendantClaimIssued;

    @NotEmpty
    private String defendantResponseIssuedToIndividual;

    @NotEmpty
    private String defendantResponseIssuedToCompany;

    @NotEmpty
    private String claimantResponseIssued;

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

    public String getClaimantMoreTimeRequested() {
        return claimantMoreTimeRequested;
    }

    public void setClaimantMoreTimeRequested(String claimantMoreTimeRequested) {
        this.claimantMoreTimeRequested = claimantMoreTimeRequested;
    }

    public String getClaimantClaimIssued() {
        return claimantClaimIssued;
    }

    public void setClaimantClaimIssued(String claimantClaimIssued) {
        this.claimantClaimIssued = claimantClaimIssued;
    }

    public String getDefendantClaimIssued() {
        return defendantClaimIssued;
    }

    public void setDefendantClaimIssued(String defendantClaimIssued) {
        this.defendantClaimIssued = defendantClaimIssued;
    }

    public String getDefendantResponseIssuedToIndividual() {
        return defendantResponseIssuedToIndividual;
    }

    public void setDefendantResponseIssuedToIndividual(String defendantResponseIssuedToIndividual) {
        this.defendantResponseIssuedToIndividual = defendantResponseIssuedToIndividual;
    }

    public String getDefendantResponseIssuedToCompany() {
        return defendantResponseIssuedToCompany;
    }

    public void setDefendantResponseIssuedToCompany(String defendantResponseIssuedToCompany) {
        this.defendantResponseIssuedToCompany = defendantResponseIssuedToCompany;
    }

    public String getClaimantResponseIssued() {
        return claimantResponseIssued;
    }

    public void setClaimantResponseIssued(String claimantResponseIssued) {
        this.claimantResponseIssued = claimantResponseIssued;
    }

    public String getStaffMoreTimeRequested() {
        return staffMoreTimeRequested;
    }

    public void setStaffMoreTimeRequested(String staffMoreTimeRequested) {
        this.staffMoreTimeRequested = staffMoreTimeRequested;
    }

    public String getDefendantMoreTimeRequested() {
        return defendantMoreTimeRequested;
    }

    public void setDefendantMoreTimeRequested(String defendantMoreTimeRequested) {
        this.defendantMoreTimeRequested = defendantMoreTimeRequested;
    }

    public String getRepresentativeClaimIssued() {
        return representativeClaimIssued;
    }

    public void setRepresentativeClaimIssued(String representativeClaimIssued) {
        this.representativeClaimIssued = representativeClaimIssued;
    }

    public String getClaimantCCJRequested() {
        return claimantCCJRequested;
    }

    public void setClaimantCCJRequested(String claimantCCJRequested) {
        this.claimantCCJRequested = claimantCCJRequested;
    }

    public String getDefendantOfferMade() {
        return defendantOfferMade;
    }

    public void setDefendantOfferMade(String defendantOfferMade) {
        this.defendantOfferMade = defendantOfferMade;
    }

    public String getClaimantOfferMade() {
        return claimantOfferMade;
    }

    public void setClaimantOfferMade(String claimantOfferMade) {
        this.claimantOfferMade = claimantOfferMade;
    }

    public String getOfferAcceptedByClaimantEmailToClaimant() {
        return offerAcceptedByClaimantEmailToClaimant;
    }

    public void setOfferAcceptedByClaimantEmailToClaimant(String offerAcceptedByClaimantEmailToClaimant) {
        this.offerAcceptedByClaimantEmailToClaimant = offerAcceptedByClaimantEmailToClaimant;
    }

    public String getOfferAcceptedByClaimantEmailToDefendant() {
        return offerAcceptedByClaimantEmailToDefendant;
    }

    public void setOfferAcceptedByClaimantEmailToDefendant(String offerAcceptedByClaimantEmailToDefendant) {
        this.offerAcceptedByClaimantEmailToDefendant = offerAcceptedByClaimantEmailToDefendant;
    }

    public String getOfferRejectedByClaimantEmailToClaimant() {
        return offerRejectedByClaimantEmailToClaimant;
    }

    public void setOfferRejectedByClaimantEmailToClaimant(String offerRejectedByClaimantEmailToClaimant) {
        this.offerRejectedByClaimantEmailToClaimant = offerRejectedByClaimantEmailToClaimant;
    }

    public String getOfferRejectedByClaimantEmailToDefendant() {
        return offerRejectedByClaimantEmailToDefendant;
    }

    public void setOfferRejectedByClaimantEmailToDefendant(String offerRejectedByClaimantEmailToDefendant) {
        this.offerRejectedByClaimantEmailToDefendant = offerRejectedByClaimantEmailToDefendant;
    }

    public String getOfferCounterSignedEmailToOriginator() {
        return offerCounterSignedEmailToOriginator;
    }

    public void setOfferCounterSignedEmailToOriginator(String offerCounterSignedEmailToOriginator) {
        this.offerCounterSignedEmailToOriginator = offerCounterSignedEmailToOriginator;
    }

    public String getOfferCounterSignedEmailToOtherParty() {
        return offerCounterSignedEmailToOtherParty;
    }

    public void setOfferCounterSignedEmailToOtherParty(String offerCounterSignedEmailToOtherParty) {
        this.offerCounterSignedEmailToOtherParty = offerCounterSignedEmailToOtherParty;
    }
}
