package uk.gov.hmcts.cmc.claimstore.models.courtfinder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    @JsonProperty("address_lines")
    private List<String> addressLines;
    private String postcode;
    private String town;
    private String type;

    @Override
    public String toString() {
        return Stream.of(addressLines, Arrays.asList(postcode, town))
            .flatMap(Collection::stream)
            .map(s -> s.replaceAll("\\r\\n|\\r|\\n", ""))
            .collect(Collectors.joining("\n"));
    }
}
