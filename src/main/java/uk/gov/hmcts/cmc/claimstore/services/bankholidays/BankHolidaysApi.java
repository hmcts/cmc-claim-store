package uk.gov.hmcts.cmc.claimstore.services.bankholidays;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "bank-holidays-api", url = "${bankHolidays.api.url}")
public interface BankHolidaysApi {

    @RequestMapping(method = RequestMethod.GET, value = "/bank-holidays.json")
    BankHolidays retrieveAll();
}
