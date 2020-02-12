package uk.gov.hmcts.cmc.domain.models.bulkprint;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BulkPrintCollection {

    private final List<BulkPrintDetails> bulkPrintDetailsList = new ArrayList<>();

    public void addBulkPrintDetails(BulkPrintDetails bulkPrintDetails) {
        bulkPrintDetailsList.add(bulkPrintDetails);
    }

    public Optional<List<BulkPrintDetails>> getBulkPrintDetails(BulkPrintLetterType bulkPrintLetterType) {
        return Optional.of(bulkPrintDetailsList);
    }
}
