package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;

import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class EmploymentMapper implements Mapper<CCDEmployment, Employment> {

    private final EmployerMapper employerMapper;
    private final SelfEmploymentMapper selfEmploymentMapper;
    private final UmemploymentMapper umemploymentMapper;

    @Autowired
    public EmploymentMapper(
        EmployerMapper employerMapper,
        SelfEmploymentMapper selfEmploymentMapper,
        UmemploymentMapper umemploymentMapper
    ) {
        this.employerMapper = employerMapper;
        this.selfEmploymentMapper = selfEmploymentMapper;
        this.umemploymentMapper = umemploymentMapper;
    }

    @Override
    public CCDEmployment to(Employment employment) {

        CCDEmployment.CCDEmploymentBuilder builder = CCDEmployment.builder();

        builder.employers(
            asStream(employment.getEmployers())
                .map(employerMapper::to)
                .map(employer -> CCDCollectionElement.<CCDEmployer>builder().value(employer).build())
                .collect(Collectors.toList()));

        employment.getSelfEmployment()
            .ifPresent(selfEmployment -> builder.selfEmployment(selfEmploymentMapper.to(selfEmployment)));

        employment.getUnemployment()
            .ifPresent(unemployment -> builder.unemployment(umemploymentMapper.to(unemployment)));

        return builder.build();
    }

    @Override
    public Employment from(CCDEmployment ccdEmployment) {
        if (ccdEmployment == null) {
            return null;
        }

        return Employment.builder()
            .selfEmployment(selfEmploymentMapper.from(ccdEmployment.getSelfEmployment()))
            .unemployment(umemploymentMapper.from(ccdEmployment.getUnemployment()))
            .employers(asStream(ccdEmployment.getEmployers())
                .map(CCDCollectionElement::getValue)
                .map(employerMapper::from)
                .collect(Collectors.toList())
            )
            .build();
    }
}
