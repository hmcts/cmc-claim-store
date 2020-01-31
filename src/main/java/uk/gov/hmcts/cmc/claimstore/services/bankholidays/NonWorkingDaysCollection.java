package uk.gov.hmcts.cmc.claimstore.services.bankholidays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.Predicate;

@Component
public class NonWorkingDaysCollection {

    private final String dataResource;

    public NonWorkingDaysCollection(@Value("${nonworking-days.datafile}") String dataSource) {
        this.dataResource = dataSource;
    }

    public boolean contains(LocalDate date) {
        final String isoDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        try {
            String data = ResourceReader.readString(dataResource);
            return Arrays.stream(data.split("[\r\n]+"))
                .map(String::trim)
                .anyMatch(Predicate.isEqual(isoDate));
        } catch (IllegalStateException e) {
            // thrown from ResourceReader#readString
            return false;
        }
    }
}
