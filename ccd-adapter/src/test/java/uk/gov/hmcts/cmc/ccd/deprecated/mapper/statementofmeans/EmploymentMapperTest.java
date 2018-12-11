package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDOnTaxPayments;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDSelfEmployment;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDUnemployed;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDUnemployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployed;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class EmploymentMapperTest {

    @Autowired
    private EmploymentMapper mapper;

    @Test
    public void shouldMapEmploymentToCCD() {
        //given
        Employment employment = Employment.builder()
            .employers(asList(Employer.builder().name("CMC").jobTitle("My sweet job").build()))
            .selfEmployment(SelfEmployment.builder()
                .jobTitle("Director")
                .annualTurnover(TEN)
                .onTaxPayments(OnTaxPayments.builder()
                    .amountYouOwe(ONE)
                    .reason("My reason")
                    .build()
                )
                .build()
            )
            .unemployment(Unemployment.builder()
                .unemployed(Unemployed.builder()
                    .numberOfYears(1)
                    .numberOfMonths(4)
                    .build())
                .retired(false)
                .build()
            )
            .build();

        //when
        CCDEmployment ccdEmployment = mapper.to(employment);

        //then
        assertThat(employment).isEqualTo(ccdEmployment);

    }

    @Test
    public void shouldMapEmploymentFromCCD() {
        //given
        CCDEmployer ccdEmployer = CCDEmployer.builder()
            .jobTitle("A job")
            .name("A Company")
            .build();

        CCDEmployment ccdEmployment = CCDEmployment.builder()
            .employers(asList(CCDCollectionElement.<CCDEmployer>builder().value(ccdEmployer).build()))
            .selfEmployment(CCDSelfEmployment.builder()
                .jobTitle("Director")
                .annualTurnover(TEN)
                .onTaxPayments(CCDOnTaxPayments.builder()
                    .amountYouOwe(ONE)
                    .reason("My reason")
                    .build()
                )
                .build()
            )
            .unemployment(CCDUnemployment.builder()
                .unemployed(CCDUnemployed.builder()
                    .numberOfYears(1)
                    .numberOfMonths(4)
                    .build())
                .build()
            )
            .build();

        //when
        Employment employment = mapper.from(ccdEmployment);

        //then
        assertThat(employment).isEqualTo(ccdEmployment);
    }
}
