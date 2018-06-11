package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount.BankAccountType.CURRENT_ACCOUNT;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount.BankAccountType.OTHER;

public class BankAccountTest {
    public static BankAccount.BankAccountBuilder newSampleOfBankAccountBuilder() {
        return BankAccount.builder()
                .balance(TEN)
                .type(CURRENT_ACCOUNT)
                .joint(false);
    }

    @Test
    public void shouldBeSuccessfulValidationForCorrectBankAccount() {
        //given
        BankAccount bankAccount = newSampleOfBankAccountBuilder().build();
        //when
        Set<String> response = validate(bankAccount);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        BankAccount bankAccount = BankAccount.builder().build();
        //when
        Set<String> errors = validate(bankAccount);
        //then
        assertThat(errors)
            .hasSize(2);
    }

    @Test
    public void shouldBeInvalidForNullTypeOfAccount() {
        //given
        BankAccount bankAccount = BankAccount.builder()
            .joint(false)
            .balance(TEN)
            .build();
        //when
        Set<String> errors = validate(bankAccount);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("type : may not be null");
    }

    @Test
    public void shouldBeInvalidForNullBalance() {
        //given
        BankAccount bankAccount = BankAccount.builder()
            .joint(true)
            .type(OTHER)
            .build();
        //when
        Set<String> errors = validate(bankAccount);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("balance : may not be null");
    }

    @Test
    public void shouldBeInvalidForBalanceWithMoreThanTwoFractions() {
        //given
        BankAccount bankAccount = BankAccount.builder()
                .joint(true)
                .type(OTHER)
                .balance(BigDecimal.valueOf(0.123f))
                .build();
        //when
        Set<String> errors = validate(bankAccount);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("balance : can not be more than 2 fractions");
    }
}
