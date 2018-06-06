package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class EmploymentMapper implements Mapper<CCDEmployment, Employment> {

    private final EmployerMapper employerMapper;
    private final SelfEmployedMapper selfEmployedMapper;

    @Autowired
    public EmploymentMapper(
        EmployerMapper employerMapper,
        SelfEmployedMapper selfEmployedMapper
    ) {
        this.employerMapper = employerMapper;
        this.selfEmployedMapper = selfEmployedMapper;
    }

    @Override
    public CCDEmployment to(Employment employment) {

        CCDEmployment.CCDEmploymentBuilder builder = CCDEmployment.builder()
            .isEmployed(CCDYesNoOption.valueOf(employment.getEmploymentOption().name()))
            .isSelfEmployed(CCDYesNoOption.valueOf(employment.getSelfEmployedOption().name()));

        builder.employers(
            asStream(employment.getEmployers())
                .map(employerMapper::to)
                .filter(Objects::nonNull)
                .map(employer -> CCDCollectionElement.<CCDEmployer>builder().value(employer).build())
                .collect(Collectors.toList()));

        employment.getSelfEmployed()
            .ifPresent(selfEmployed -> builder.selfEmployed(selfEmployedMapper.to(selfEmployed)));

        return builder.build();
    }

    @Override
    public Employment from(CCDEmployment ccdEmployment) {
        if (ccdEmployment == null) {
            return null;
        }

        List<Employer> employers = asStream(ccdEmployment.getEmployers())
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(employerMapper::from)
            .collect(Collectors.toList());

        return new Employment(
            YesNoOption.valueOf(ccdEmployment.getIsEmployed().name()),
            YesNoOption.valueOf(ccdEmployment.getIsSelfEmployed().name()),
            employers,
            selfEmployedMapper.from(ccdEmployment.getSelfEmployed())
        );
    }
}
