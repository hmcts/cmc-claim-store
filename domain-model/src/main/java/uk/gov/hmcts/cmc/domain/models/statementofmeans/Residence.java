package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(value = "typeDescription", allowGetters = true)
@Builder
public class Residence {

    public enum ResidenceType {
        OWN_HOME("Home you own yourself (or pay a mortgage on)"),
        JOINT_OWN_HOME("Jointly-owned home (or jointly mortgaged home)"),
        PRIVATE_RENTAL("Private rental"),
        COUNCIL_OR_HOUSING_ASSN_HOME("Council or housing association home"),
        OTHER("Other");

        String description;

        ResidenceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }

    @NotNull
    private final ResidenceType type;

    private final String otherDetail;

    public Residence(ResidenceType type, String otherDetail) {
        this.type = type;
        this.otherDetail = otherDetail;
    }

    public ResidenceType getType() {
        return type;
    }

    public String getTypeDescription() {
        return type.description;
    }

    public Optional<String> getOtherDetail() {
        return Optional.ofNullable(otherDetail);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Residence residence = (Residence) other;
        return type == residence.type
            && Objects.equals(otherDetail, residence.otherDetail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, otherDetail);
    }
}
