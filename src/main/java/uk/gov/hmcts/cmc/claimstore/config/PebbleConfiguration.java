package uk.gov.hmcts.cmc.claimstore.config;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.StringLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PebbleConfiguration {

    @Bean
    public PebbleEngine pebbleEngine() {
        return new PebbleEngine.Builder()
            .loader(new StringLoader())
            .cacheActive(false)
            .build();
    }

}
