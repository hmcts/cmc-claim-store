package uk.gov.hmcts.cmc.claimstore.config.properties.notifications;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {

    @NotEmpty
    private String claimantClaimIssued;

    @NotEmpty
    private String defendantClaimIssued;

    @NotEmpty
    private String defendantResponseIssued;

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

    public String getDefendantResponseIssued() {
        return defendantResponseIssued;
    }

    public void setDefendantResponseIssued(String defendantResponseIssued) {
        this.defendantResponseIssued = defendantResponseIssued;
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

    public void setRepresentativeClaimIssued(final String representativeClaimIssued) {
        this.representativeClaimIssued = representativeClaimIssued;
    }
}
