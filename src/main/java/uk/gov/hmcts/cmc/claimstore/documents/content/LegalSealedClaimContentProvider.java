package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class LegalSealedClaimContentProvider {

    private final StatementOfValueProvider statementOfValueProvider;
    private final boolean watermarkPdf;

    @Autowired
    public LegalSealedClaimContentProvider(StatementOfValueProvider statementOfValueProvider,
                                           @Value("${feature_toggles.watermark_pdf}") boolean watermarkPdf) {
        this.statementOfValueProvider = statementOfValueProvider;
        this.watermarkPdf = watermarkPdf;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);

        Map<String, Object> content = new HashMap<>();
        content.put("claimReferenceNumber", claim.getReferenceNumber());
        content.put("claimSubmittedOn", formatDate(claim.getCreatedAt()));
        content.put("claimIssuedOn", formatDate(claim.getIssuedOn()));
        claim.getClaimData().getFeeAccountNumber().ifPresent(f -> content.put("feeAccountNumber", f));
        content.put("claimants", claim.getClaimData().getClaimants());
        content.put("claimantsCount", claim.getClaimData().getClaimants().size());
        content.put("defendants", claim.getClaimData().getDefendants());
        content.put("defendantsCount", claim.getClaimData().getDefendants().size());
        content.put("claimSummary", claim.getClaimData().getReason());
        content.put("externalReferenceNumber", claim.getClaimData().getExternalReferenceNumber());

        Representative legalRepresentative = claim.getClaimData().getClaimants().stream()
            .findFirst().orElseThrow(IllegalArgumentException::new)
            .getRepresentative().orElseThrow(IllegalArgumentException::new);

        content.put("preferredCourt", claim.getClaimData().getPreferredCourt());
        content.put("feePaid", formatMoney(claim.getClaimData().getFeesPaidInPounds().orElse(ZERO)));
        StatementOfTruth statementOfTruth = claim.getClaimData()
            .getStatementOfTruth()
            .orElseThrow(IllegalArgumentException::new);
        content.put("signerName", statementOfTruth.getSignerName());
        content.put("signerRole", statementOfTruth.getSignerRole());
        content.put("signerCompany", legalRepresentative.getOrganisationName());
        content.put("statementOfValue", statementOfValueProvider.create(claim));
        content.put("watermarkPdf", watermarkPdf);

        return content;
    }
}
