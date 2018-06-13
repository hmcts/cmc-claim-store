package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;

import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class EmploymentMapper implements Mapper<CCDEmployment, Employment> {

    private final EmployerMapper employerMapper;
    private final SelfEmploymentMapper selfEmploymentMapper;
    private final UmemploymentMapper unEmploymentMapper;

    @Autowired
    public EmploymentMapper(
        EmployerMapper employerMapper,
        SelfEmploymentMapper selfEmploymentMapper,
        UmemploymentMapper unEmploymentMapper
    ) {
        this.employerMapper = employerMapper;
        this.selfEmploymentMapper = selfEmploymentMapper;
        this.unEmploymentMapper = unEmploymentMapper;
    }

    @Override
    public CCDEmployment to(Employment employment) {

        CCDEmployment.CCDEmploymentBuilder builder = CCDEmployment.builder();

        builder.employers(
            asStream(employment.getEmployers())
                .map(employerMapper::to)
                .filter(Objects::nonNull)
                .map(employer -> CCDCollectionElement.<CCDEmployer>builder().value(employer).build())
                .collect(Collectors.toList()));

        employment.getSelfEmployment()
            .ifPresent(selfEmployment -> builder.selfEmployment(selfEmploymentMapper.to(selfEmployment)));

        employment.getUnemployment().ifPresent(unemployment -> unEmploymentMapper.to(unemployment));

        return builder.build();
    }

    @Override
    public Employment from(CCDEmployment ccdEmployment) {
        if (ccdEmployment == null) {
            return null;
        }

        return Employment.builder()
            .selfEmployment(selfEmploymentMapper.from(ccdEmployment.getSelfEmployment()))
            .unemployment(unEmploymentMapper.from(ccdEmployment.getUnemployment()))
            .employers(asStream(ccdEmployment.getEmployers())
                .map(CCDCollectionElement::getValue)
                .filter(Objects::nonNull)
                .map(employerMapper::from)
                .collect(Collectors.toList())
            )
            .build();
    }
}
