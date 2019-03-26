package uk.gov.hmcts.cmc.claimstore.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.MediationCSVGenerationException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.MediationRow;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MediationCSVService {

    private static final int MEDIATION_CLAIMANT_PARTY_TYPE = 1;
    private static final int MEDIATION_DEFENDANT_PARTY_TYPE = 2;

    private CaseRepository caseRepository;

    @Autowired
    public MediationCSVService(
        CaseRepository caseRepository
    ) {
        this.caseRepository = caseRepository;
    }

    public String createMediationCSV(String authorisation, LocalDate mediationDate) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.DEFAULT.withNullString("null"));
            csvPrinter.printRecords(createMediationRowForEachParty(authorisation, mediationDate));
            csvPrinter.flush();
        } catch (IOException e) {
            throw new MediationCSVGenerationException("Error generating Mediation CSV");
        }
        return stringBuilder.toString();
    }

    private List<List<String>> createMediationRowForEachParty(String authorisation, LocalDate mediationDate) {
        List<Claim> mediationClaims = caseRepository.getMediationClaims(authorisation, mediationDate);
        List<List<String>> result = new ArrayList<>();

        if(mediationClaims.isEmpty()) {
            result.add(MediationRow.builder()
                .siteId(null)
                .caseType(null)
                .checkList(null)
                .partyStatus(null)
                .build().toArray()
            );
        }

        mediationClaims.forEach(claim -> {
            result.add(createMediationRow(claim, MEDIATION_CLAIMANT_PARTY_TYPE).toArray());
            result.add(createMediationRow(claim, MEDIATION_DEFENDANT_PARTY_TYPE).toArray());
        });

        return result;
    }

    private MediationRow createMediationRow (Claim claim, int partyType) {
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        return MediationRow.builder()
            .caseNumber(claim.getReferenceNumber())
            .amount(String.valueOf(claim.getTotalAmountTillToday().orElse(BigDecimal.ZERO)))
            .partyType(String.valueOf(partyType))
            .contactName(response.getMediationContactPerson().orElse(Strings.EMPTY))
            .contactNumber(response.getMediationPhoneNumber().orElse(Strings.EMPTY))
            .build();
    }
}
