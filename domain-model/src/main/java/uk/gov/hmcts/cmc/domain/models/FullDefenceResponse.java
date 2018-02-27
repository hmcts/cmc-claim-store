package uk.gov.hmcts.cmc.domain.models;

import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class FullDefenceResponse extends Response {

    public enum DefenceType {
        DISPUTE,
        ALREADY_PAID
    }

    @NotNull
    private final DefenceType defenceType;

    @NotBlank
    @Size(max = 99000)
    private final String defence;

    @Valid
    private final WhenDidYouPay whenDidYouPay;

    public FullDefenceResponse(
        FreeMediationOption freeMediation,
        MoreTimeNeededOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        DefenceType defenceType,
        String defence,
        WhenDidYouPay whenDidYouPay
    ) {
        super(freeMediation, moreTimeNeeded, defendant, statementOfTruth);
        this.defenceType = defenceType;
        this.defence = defence;
        this.whenDidYouPay = whenDidYouPay;
    }

    public DefenceType getDefenceType() {
        return defenceType;
    }

    public String getDefence() {
        return defence;
    }

    public WhenDidYouPay getWhenDidYouPay() { return whenDidYouPay; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        FullDefenceResponse other = (FullDefenceResponse) obj;

        return super.equals(other)
            && Objects.equals(defenceType, other.defenceType)
            && Objects.equals(defence, other.defence)
            && Objects.equals(whenDidYouPay, other.whenDidYouPay);

    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), defenceType, defence, whenDidYouPay);
    }

}
