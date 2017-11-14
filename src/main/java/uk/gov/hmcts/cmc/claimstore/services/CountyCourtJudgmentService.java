package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.EmailContentTemplates;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.ContentProvider;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

@Component
public class CountyCourtJudgmentService {

    private final ClaimService claimService;
    private final EventProducer eventProducer;
    private final PDFServiceClient pdfServiceClient;
    private final EmailContentTemplates emailTemplates;
    private final ContentProvider contentProvider;

    @Autowired
    public CountyCourtJudgmentService(
        final ClaimService claimService,
        final EventProducer eventProducer,
        final PDFServiceClient pdfServiceClient,
        final EmailContentTemplates emailTemplates,
        final ContentProvider contentProvider
    ) {
        this.claimService = claimService;
        this.eventProducer = eventProducer;
        this.pdfServiceClient = pdfServiceClient;
        this.emailTemplates = emailTemplates;
        this.contentProvider = contentProvider;
    }

    @Transactional
    public Claim save(final String submitterId, final CountyCourtJudgment countyCourtJudgment, final long claimId) {

        Claim claim = claimService.getClaimById(claimId);

        if (!isClaimSubmittedByUser(claim, submitterId)) {
            throw new ForbiddenActionException("Claim " + claimId + " does not belong to user" + submitterId);
        }

        if (isResponseAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("Response for the claim " + claimId + " was submitted");
        }

        if (isCountyCourtJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("County Court Judgment for the claim " + claimId + " was submitted");
        }

        if (!canCountyCourtJudgmentBeRequestedYet(claim)) {
            throw new ForbiddenActionException(
                "County Court Judgment for claim " + claimId + " cannot be requested yet"
            );
        }

        claimService.saveCountyCourtJudgment(claimId, countyCourtJudgment);

        Claim claimWithCCJ = claimService.getClaimById(claimId);

        eventProducer.createCountyCourtJudgmentRequestedEvent(claimWithCCJ);

        return claimWithCCJ;
    }

    private boolean canCountyCourtJudgmentBeRequestedYet(final Claim claim) {
        return LocalDate.now().isAfter(claim.getResponseDeadline());
    }

    private boolean isResponseAlreadySubmitted(final Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isClaimSubmittedByUser(final Claim claim, final String submitterId) {
        return claim.getSubmitterId().equals(submitterId);
    }

    private boolean isCountyCourtJudgmentAlreadySubmitted(final Claim claim) {
        return claim.getCountyCourtJudgment() != null;
    }

    public byte[] createPdf(Claim claim) {
        requireNonNull(claim);
        return pdfServiceClient.generateFromHtml(
            emailTemplates.getCountyCourtJudgmentDetails(),
            this.contentProvider.createContent(claim)
        );
    }
}
