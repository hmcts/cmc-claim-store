package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import uk.gov.hmcts.cmc.claimstore.exceptions.MediationCSVGenerationException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.MediationRow;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.domain.models.MediationRow.MediationRowBuilder;

@RequiredArgsConstructor
public class MediationCSVGenerator {
    private static final String SITE_ID = "4";
    private static final String CASE_TYPE = "1";
    private static final String CHECK_LIST = "4";
    private static final String PARTY_STATUS = "5";

    private static final int CLAIMANT_PARTY_TYPE = 1;
    private static final int DEFENDANT_PARTY_TYPE = 2;

    private static final Map<Integer, Function<Claim, Optional<String>>> CONTACT_PERSON_EXTRACTORS =
        ImmutableMap.of(
            CLAIMANT_PARTY_TYPE,
            claim -> claim.getClaimantResponse()
                .filter(ResponseRejection.class::isInstance)
                .map(ResponseRejection.class::cast)
                .orElseThrow(invalidMediationStateException("Missing rejection response"))
                .getMediationContactPerson(),

            DEFENDANT_PARTY_TYPE,
            claim -> claim.getResponse()
                .orElseThrow(invalidMediationStateException("Missing response data"))
                .getMediationContactPerson()
        );

    private static final Map<Integer, Function<Claim, Optional<String>>> CONTACT_NUMBER_EXTRACTORS =
        ImmutableMap.of(
            CLAIMANT_PARTY_TYPE,
            claim -> claim.getClaimantResponse()
                .filter(ResponseRejection.class::isInstance)
                .map(ResponseRejection.class::cast)
                .orElseThrow(invalidMediationStateException("Missing rejection response"))
                .getMediationPhoneNumber(),

            DEFENDANT_PARTY_TYPE,
            claim -> claim.getResponse()
                .orElseThrow(invalidMediationStateException("Missing response data"))
                .getMediationPhoneNumber()
        );

    private static final String NULL_STRING = "null";

    private final CaseRepository caseRepository;

    private final LocalDate mediationDate;

    private final String authorisation;

    @Getter
    private String csvData;

    @Getter
    private Map<String, String> problematicRecords = new HashMap<>();

    public void createMediationCSV() {
        problematicRecords.clear();
        StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.DEFAULT.withNullString(NULL_STRING))) {
            csvPrinter.printRecords(createMediationRowForEachParty());
            csvPrinter.flush();
            csvData = stringBuilder.toString();
        } catch (Exception e) {
            throw new MediationCSVGenerationException("Error generating Mediation CSV", e);
        }
    }

    private List<MediationRow> createMediationRowForEachParty() {
        List<MediationRow> result = caseRepository.getMediationClaims(authorisation, mediationDate)
            .stream()
            .map(this::createMediationRows)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        if (result.isEmpty()) {
            result.add(MediationRow.builder().build());
        }

        return result;
    }

    private List<MediationRow> createMediationRows(Claim claim) {
        try {
            return Arrays.asList(
                createMediationRow(claim, CLAIMANT_PARTY_TYPE),
                createMediationRow(claim, DEFENDANT_PARTY_TYPE)
            );
        } catch (MediationCSVGenerationException e) {
            problematicRecords.put(claim.getReferenceNumber(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private MediationRow createMediationRow(Claim claim, int partyType) {
        MediationRowBuilder mediationRowBuilder = MediationRow.builder()
            .siteId(SITE_ID)
            .caseType(CASE_TYPE)
            .checkList(CHECK_LIST)
            .partyStatus(PARTY_STATUS)
            .caseNumber(claim.getReferenceNumber())
            .amount(String.valueOf(claim.getTotalAmountTillToday()
                .orElseThrow(invalidMediationStateException("Unable to find total amount of claim"))))
            .partyType(String.valueOf(partyType));

        CONTACT_PERSON_EXTRACTORS.get(partyType)
            .apply(claim)
            .ifPresent(mediationRowBuilder::contactName);
        CONTACT_NUMBER_EXTRACTORS.get(partyType)
            .apply(claim)
            .ifPresent(mediationRowBuilder::contactNumber);

        return mediationRowBuilder.build();
    }

    private static Supplier<RuntimeException> invalidMediationStateException(String reason) {
        return () -> new MediationCSVGenerationException(reason);
    }
}
