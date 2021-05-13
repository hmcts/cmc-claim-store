package uk.gov.hmcts.cmc.domain.models.orders;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@EqualsAndHashCode
@Getter
public class BespokeOrderDirection {
    private final List<BespokeDirection> bespokeDirections = new ArrayList<>();
    private boolean bespokeDirectionOrderWarning;

    public void addBespokeDirection(BespokeDirection bespokeDirection) {
        bespokeDirections.add(bespokeDirection);
    }
}
