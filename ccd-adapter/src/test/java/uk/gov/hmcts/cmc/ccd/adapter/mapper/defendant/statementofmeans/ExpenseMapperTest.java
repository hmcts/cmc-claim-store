package uk.gov.hmcts.cmc.ccd.adapter.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPaymentFrequency;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.adapter.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpenseType.COUNCIL_TAX;
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
            .amount(BigDecimal.TEN)
            .build();

        //when
        CCDCollectionElement<CCDExpense> ccdExpense = mapper.to(expense);

        //then
        assertThat(expense).isEqualTo(ccdExpense.getValue());
        assertThat(expense.getId()).isEqualTo(ccdExpense.getId());
    }

    @Test
    public void shouldMapIncomeFromCCD() {
        //given
        CCDExpense ccdExpense = CCDExpense.builder()
            .type(COUNCIL_TAX)
            .frequency(CCDPaymentFrequency.MONTH)
            .amountPaid("1000")
            .build();

        String collectionId = UUID.randomUUID().toString();

        //when
        Expense expense = mapper.from(CCDCollectionElement.<CCDExpense>builder()
            .id(collectionId)
            .value(ccdExpense).build());

        //then
        assertThat(expense).isEqualTo(ccdExpense);
        assertThat(expense.getId()).isEqualTo(collectionId);
    }
}
