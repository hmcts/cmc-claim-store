package uk.gov.hmcts.cmc.claimstore.services.bankholidays;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.PublicHolidaysCollection.Endpoints;

@FeignClient(name = "bank-holidays-api", url = "${bankHolidays.api.url}")
public interface BankHolidaysApi {

    @RequestMapping(method = RequestMethod.GET, value = Endpoints.BANK_HOLIDAYS)
    BankHolidays retrieveAll();
}
