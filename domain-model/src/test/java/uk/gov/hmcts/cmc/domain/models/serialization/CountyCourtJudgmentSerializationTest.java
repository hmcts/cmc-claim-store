package uk.gov.hmcts.cmc.domain.models.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class CountyCourtJudgmentSerializationTest {

    private static final ObjectMapper mapper = new JacksonConfiguration().objectMapper();

    @Test
    public void shouldConvertCCJJsonToJava() throws IOException {

        //given
        CompanyDetails defendant = SampleTheirDetails.builder().companyDetails();
        CountyCourtJudgment expected = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .paidAmount(BigDecimal.ZERO)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .statementOfTruth(new StatementOfTruth(defendant.getContactPerson().orElse(null), "Director"))
            .build();

        //when
        CountyCourtJudgment other = jsonToModel("/county-court-judgment/by-instalments.json");

        //then
        assertThat(expected).isEqualTo(other);
    }

    private static CountyCourtJudgment jsonToModel(String path) throws IOException {
        String json = new ResourceReader().read(path);
        return mapper.readValue(json, CountyCourtJudgment.class);
    }
}
