package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDChild;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDependant;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDOtherDependants;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OtherDependants;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DependantMapper implements Mapper<CCDDependant, Dependant> {

    private final ChildMapper childMapper;

    @Autowired
    public DependantMapper(ChildMapper childMapper) {
        this.childMapper = childMapper;
    }

    @Override
    public CCDDependant to(Dependant dependant) {
        if (dependant == null) {
            return null;
        }

        CCDDependant.CCDDependantBuilder builder = CCDDependant.builder()
            .numberOfMaintainedChildren(dependant.getNumberOfMaintainedChildren().orElse(null));

        builder.children(dependant.getChildren().stream().map(childMapper::to)
            .map(this::mapToValue)
            .collect(Collectors.toList()));

        dependant.getOtherDependants().ifPresent(otherDependants ->
            builder.otherDependants(CCDOtherDependants.builder()
                .details(otherDependants.getDetails())
                .numberOfPeople(otherDependants.getNumberOfPeople())
                .build())
        );

        return builder.build();
    }

    private CCDCollectionElement<CCDChild> mapToValue(CCDChild ccdChild) {
        return CCDCollectionElement.<CCDChild>builder().value(ccdChild).build();
    }

    @Override
    public Dependant from(CCDDependant ccdDependant) {
        if (ccdDependant == null) {
            return null;
        }

        List<Child> children = ccdDependant.getChildren()
            .stream()
            .map(CCDCollectionElement::getValue)
            .map(childMapper::from)
            .collect(Collectors.toList());

        OtherDependants otherDependants = mapOtherDependant(ccdDependant.getOtherDependants());

        return new Dependant(
            children,
            ccdDependant.getNumberOfMaintainedChildren(),
            otherDependants
        );
    }

    private OtherDependants mapOtherDependant(CCDOtherDependants ccdOtherDependants) {
        if (ccdOtherDependants == null) {
            return null;
        }

        return new OtherDependants(ccdOtherDependants.getNumberOfPeople(), ccdOtherDependants.getDetails());
    }

}
