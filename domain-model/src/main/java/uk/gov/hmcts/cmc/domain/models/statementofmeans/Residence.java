package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.cmc.domain.constraints.ValidResidence;

import java.util.Optional;
import javax.validation.constraints.NotNull;

@Builder
@EqualsAndHashCode
@ValidResidence
public class Residence {

    public enum ResidenceType {
        OWN_HOME("Home you own yourself (or pay a mortgage on)"),
        JOINT_OWN_HOME("Jointly-owned home (or jointly mortgaged home)"),
        PRIVATE_RENTAL("Private rental"),
        COUNCIL_OR_HOUSING_ASSN_HOME("Council or housing association home"),
        OTHER("Other");

        private final String description;

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

    public Optional<String> getOtherDetail() {
        return Optional.ofNullable(otherDetail);
    }

}
