package uk.gov.hmcts.cmc.ccd-adapter.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class MoneyMapperTest {

    @Autowired
    private MoneyMapper mapper;

    @Test
    public void shouldMapToCCD() {
        //Given
        BigDecimal amountInPounds = BigDecimal.valueOf(123.50);
        //when
        String amountInPennies = mapper.to(amountInPounds);

        //then
        assertMoney(amountInPounds).isEqualTo(amountInPennies);
    }

    @Test
    public void shouldMapFromCCD() {
        //Given
        String amountInPennies = "12350";
        //when
        BigDecimal amountInPounds = mapper.from(amountInPennies);

        //then
        assertMoney(amountInPounds).isEqualTo(amountInPennies);
    }
}
