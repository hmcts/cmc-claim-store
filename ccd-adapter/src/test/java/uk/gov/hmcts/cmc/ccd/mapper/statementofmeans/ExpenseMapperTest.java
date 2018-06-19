package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense.ExpenseType.COUNCIL_TAX;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense.ExpenseType.MORTGAGE;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency.MONTH;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ExpenseMapperTest {

    @Autowired
    private ExpenseMapper mapper;

    @Test
    public void shouldMapExpenseToCCD() {
        //given
        Expense expense = Expense.builder()
            .type(MORTGAGE)
            .frequency(MONTH)
            .amountPaid(BigDecimal.TEN)
            .build();

        //when
        CCDExpense ccdExpense = mapper.to(expense);

        //then
        assertThat(expense).isEqualTo(ccdExpense);
    }

    @Test
    public void shouldMapIncomeFromCCD() {
        //given
        CCDExpense ccdExpense = CCDExpense.builder()
            .type(COUNCIL_TAX)
            .frequency(MONTH)
            .amountPaid(BigDecimal.TEN)
            .build();

        //when
        Expense expense = mapper.from(ccdExpense);

        //then
        assertThat(expense).isEqualTo(ccdExpense);
    }
}
