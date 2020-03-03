package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Postcode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder()
@EqualsAndHashCode
public class Address {

    @NotBlank(message = "Address Line1 should not be empty")
    @Size(max = 100, message = "Address Line1 should not be longer than {max} characters")
    private final String line1;

    @Size(max = 100, message = "Address Line2 should not be longer than {max} characters")
    private final String line2;

    @Size(max = 100, message = "Address Line3 should not be longer than {max} characters")
    private final String line3;

    @NotBlank(message = "City/town should not be empty")
    @Size(max = 100, message = "City should not be longer than {max} characters")
    private final String city;

    @Size(max = 100, message = "County should not be longer than {max} characters")
    private final String county;

    @NotNull
    @Postcode
    private final String postcode;

    public Address(String line1,
                   String line2,
                   String line3,
                   String city,
                   String county,
                   String postcode) {
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.city = city;
        this.county = county;
        this.postcode = postcode;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getLine3() {
        return line3;
    }

    public String getCity() {
        return city;
    }

    public String getCounty() {
        return county;
    }

    public String getPostcode() {
        return postcode;
    }

    @Override
    public String toString() {
        return "Address{"
            + "line1='" + line1 + '\''
            + ", line2='" + line2 + '\''
            + ", line3='" + line3 + '\''
            + ", city='" + city + '\''
            + ", county='" + county + '\''
            + ", postcode='" + postcode + '\''
            + '}';
    }
}
