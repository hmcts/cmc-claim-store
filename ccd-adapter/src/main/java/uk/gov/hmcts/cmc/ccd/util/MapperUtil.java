package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.MediationOutcome;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_DISPUTES_ALL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_FULL_ADMIT;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_MORE_TIME;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_PART_ADMIT;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_STATES_PAID;
import static uk.gov.hmcts.cmc.ccd.util.PartyNameUtils.getPartyNameFor;
import static uk.gov.hmcts.cmc.ccd.util.PartyNameUtils.getTheirDetailsNameFor;
import static uk.gov.hmcts.cmc.domain.models.MediationOutcome.FAILED;
import static uk.gov.hmcts.cmc.domain.models.MediationOutcome.SUCCEEDED;

public class MapperUtil {
    private static final String OTHERS = " + others";

    private static final List<CCDClaimDocumentType> paperResponseDocTypes = Arrays.asList(PAPER_RESPONSE_FULL_ADMIT,
        PAPER_RESPONSE_PART_ADMIT,
        PAPER_RESPONSE_STATES_PAID,
        PAPER_RESPONSE_MORE_TIME,
        PAPER_RESPONSE_DISPUTES_ALL);

    private static final List<String> paperResponseScannedType = Arrays.asList("N9a",
        "N9b",
        "N11",
        "N225",
        "N180");

    public static final Function<Claim, String> toCaseName = claim ->
        fetchClaimantName(claim) + " Vs " + fetchDefendantName(claim);

    private static final Predicate<CCDClaimDocument> filterStaffUploadedPaperResponseDoc = doc ->
        paperResponseDocTypes.stream().anyMatch(type -> type.equals(doc.getDocumentType()));

    private static final Predicate<CCDScannedDocument> filterCaseDocumentsPaperResponseDoc = doc ->
        paperResponseScannedType.stream().anyMatch(type -> type.equalsIgnoreCase(doc.getSubtype()));

    public static final Function<CCDCase, YesNoOption> hasPaperResponse = ccdCase ->
        StreamUtil.asStream(ccdCase.getStaffUploadedDocuments())
            .map(CCDCollectionElement::getValue)
            .anyMatch(filterStaffUploadedPaperResponseDoc)
            || StreamUtil.asStream(ccdCase.getScannedDocuments()).map(CCDCollectionElement::getValue)
            .anyMatch(filterCaseDocumentsPaperResponseDoc) ? YesNoOption.YES : YesNoOption.NO;

    private MapperUtil() {
        // Utility class, no instances
    }

    public static MediationOutcome getMediationOutcome(CCDCase ccdCase) {

        CCDRespondent defendant = ccdCase.getRespondents().stream()
            .findFirst()
            .map(CCDCollectionElement::getValue)
            .orElseThrow(() -> new IllegalStateException("Missing respondent"));

        if (Optional.ofNullable(defendant.getMediationFailedReason()).isPresent()) {
            return FAILED;
        } else if (Optional.ofNullable(defendant.getMediationSettlementReachedAt()).isPresent()) {
            return SUCCEEDED;
        } else {
            return null;
        }
    }

    public static boolean isAnyNotNull(Object... objects) {
        return Stream.of(objects).anyMatch(Objects::nonNull);
    }

    private static String fetchDefendantName(Claim claim) {
        StringBuilder defendantNameBuilder = new StringBuilder();

        defendantNameBuilder.append(claim.getResponse().map(Response::getDefendant)
            .map(Party::getName)
            .orElseGet(() -> getTheirDetailsNameFor(claim.getClaimData().getDefendants().get(0))));

        if (claim.getClaimData().getDefendants().size() > 1) {
            defendantNameBuilder.append(OTHERS);
        }

        return defendantNameBuilder.toString();

    }

    private static String fetchClaimantName(Claim claim) {
        StringBuilder claimantNameBuilder = new StringBuilder();

        claimantNameBuilder.append(getPartyNameFor(claim.getClaimData().getClaimants().get(0)));

        if (claim.getClaimData().getClaimants().size() > 1) {
            claimantNameBuilder.append(OTHERS);
        }
        return claimantNameBuilder.toString();
    }
}
