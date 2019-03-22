package uk.gov.hmcts.cmc.claimstore.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.MediationRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Mediation;
import uk.gov.hmcts.cmc.domain.models.MediationRequest;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MediationService {

    private static int CLAIMANT_PARTY_TYPE = 1;
    private static int DEFENDANT_PARTY_TYPE = 2;

    private final MediationRepository mediationRepository;

    @Autowired
    public MediationService(MediationRepository mediationRepository) {
        this.mediationRepository = mediationRepository;
    }

    public void sendMediationExtract(MediationRequest mediationRequest, String authorisation) throws IOException {
        generateMediationCSV(mediationRequest.getMediationGenerateDate(), authorisation);
    }

    public void generateMediationCSV(LocalDate mediationDate, String authorisation) throws IOException {
        try (
        CSVPrinter csvPrinter = new CSVPrinter(new StringBuilder(), CSVFormat.DEFAULT)
        ) {
            csvPrinter.printRecord(generateMediationExtract(authorisation, mediationDate));
        }
    }

    public List<Mediation> generateMediationExtract(String authorisation, LocalDate mediationDate) {
        List<Claim> mediationClaims = mediationRepository.getMediationClaims(authorisation, mediationDate);

        List<Mediation> mediationsInADay = new ArrayList<>();

        for (Claim claim : mediationClaims) {
            mediationsInADay.add(getMediationDetails(claim, CLAIMANT_PARTY_TYPE));
            mediationsInADay.add(getMediationDetails(claim, DEFENDANT_PARTY_TYPE));
        }
        return mediationsInADay;
    }

    private Mediation getMediationDetails(Claim claim, int partyType) {
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        return Mediation.builder()
            .caseNumber(claim.getReferenceNumber())
            .amount(claim.getTotalAmountTillToday().orElse(BigDecimal.ZERO))
            .partyType(partyType)
            .contactName(response.getMediationContactPerson().orElse(Strings.EMPTY))
            .contactNumber(response.getMediationPhoneNumber().orElse(Strings.EMPTY))
            .build();
    }
}
