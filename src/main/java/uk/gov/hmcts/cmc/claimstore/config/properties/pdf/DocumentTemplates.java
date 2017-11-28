package uk.gov.hmcts.cmc.claimstore.config.properties.pdf;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class DocumentTemplates {

    public byte[] getDefendantResponseCopy() {
        return readBytes("/staff/templates/document/defendantResponseCopy.html");
    }

    public byte[] getDefendantResponseReceipt() {
        return readBytes("/staff/templates/document/defendantResponseReceipt.html");
    }

    public byte[] getClaimIssueReceipt() {
        return readBytes("/staff/templates/document/claimIssueReceipt.html");
    }

    public byte[] getSealedClaim() {
        return readBytes("/staff/templates/document/sealedClaim.html");
    }

    public byte[] getDefendantPinLetter() {
        return readBytes("/staff/templates/document/defendantPinLetter.html");
    }

    public byte[] getLegalSealedClaim() {
        return readBytes("/staff/templates/document/legalSealedClaim.html");
    }

    public byte[] getCountyCourtJudgmentDetails() {
        return readBytes("/staff/templates/document/countyCourtJudgmentDetails.html");
    }

    public byte[] getSettlementAgreement() {
        return readBytes("/staff/templates/document/settlementAgreement.html");
    }

    private byte[] readBytes(final String resourcePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
