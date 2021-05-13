package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDBespokeOrderDirection;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.orders.BespokeDirection;
import uk.gov.hmcts.cmc.domain.models.orders.BespokeOrderDirection;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionParty;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class BespokeOrderDirectionMapper {

    public void from(CCDCase ccdCase, Claim.ClaimBuilder claimBuilder) {
        List<CCDCollectionElement<CCDBespokeOrderDirection>> ccdBespokeOrderDirections
            = ccdCase.getBespokeDirectionList();
        if (ccdBespokeOrderDirections == null || ccdCase.getBespokeDirectionList().isEmpty()) {
            return;
        }

        BespokeOrderDirection.BespokeOrderDirectionBuilder builder = BespokeOrderDirection.builder();

        BespokeOrderDirection bespokeOrderDirection = builder
            .build();

        asStream(ccdCase.getBespokeDirectionList())
            .map(CCDCollectionElement::getValue)
            .forEach(ccdBespokeOrder -> bespokeOrderDirection.addBespokeDirection(BespokeDirection.builder()
                .beSpokeDirectionExplain(ccdBespokeOrder.getBeSpokeDirectionExplain())
                .beSpokeDirectionFor(Optional.ofNullable(ccdBespokeOrder.getBeSpokeDirectionFor())
                    .map(partyType -> DirectionParty.valueOf(partyType.name()))
                    .orElse(null))
                .beSpokeDirectionDatetime(ccdBespokeOrder.getBeSpokeDirectionDatetime())
                .build()));

        claimBuilder.bespokeOrderDirection(bespokeOrderDirection);
    }
}
