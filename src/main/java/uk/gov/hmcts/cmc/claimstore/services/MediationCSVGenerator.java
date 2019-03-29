package uk.gov.hmcts.cmc.claimstore.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.exceptions.MediationCSVGenerationException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.MediationRow;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.cmc.domain.models.MediationRow.*;

@Component
public class MediationCSVGenerator {

    private static final int MEDIATION_CLAIMANT_PARTY_TYPE = 1;
    private static final int MEDIATION_DEFENDANT_PARTY_TYPE = 2;
    public static final String NULL_STRING = "null";

    private CaseRepository caseRepository;

    @Autowired
    public MediationCSVGenerator(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public String createMediationCSV(String authorisation, LocalDate mediationDate) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.DEFAULT.withNullString(NULL_STRING));
            csvPrinter.printRecords(createMediationRowForEachParty(authorisation, mediationDate));
            csvPrinter.flush();
            return stringBuilder.toString();
        } catch (Exception e) {
            throw new MediationCSVGenerationException("Error generating Mediation CSV");
        }
    }

    private List<List<String>> createMediationRowForEachParty(String authorisation, LocalDate mediationDate) {
        List<List<String>> result = new ArrayList<>();
        List<Claim> mediationClaims = caseRepository.getMediationClaims(authorisation, mediationDate);

        if (mediationClaims.isEmpty()) {
            result.add(MediationRow.builder().build().toList());
        } else {
            mediationClaims.forEach(claim -> {
                result.add(createMediationRow(claim, MEDIATION_CLAIMANT_PARTY_TYPE).toList());
                result.add(createMediationRow(claim, MEDIATION_DEFENDANT_PARTY_TYPE).toList());
            });
        }

        return result;
    }

    private MediationRow createMediationRow(Claim claim, int partyType) {
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);

        MediationRowBuilder mediationRowBuilder = MediationRow.builder()
            .siteId(SITE_ID)
            .caseType(CASE_TYPE)
            .checkList(CHECK_LIST)
            .partyStatus(PARTY_STATUS)
            .caseNumber(claim.getReferenceNumber())
            .amount(String.valueOf(claim.getTotalAmountTillToday().orElse(BigDecimal.ZERO)))
            .partyType(String.valueOf(partyType));

        response.getMediationContactPerson().ifPresent(mediationRowBuilder::contactName);
        response.getMediationPhoneNumber().ifPresent(mediationRowBuilder::contactNumber);

        return mediationRowBuilder.build();
    }
}
