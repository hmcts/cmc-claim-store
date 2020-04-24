package uk.gov.hmcts.cmc.claimstore.services;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notNull;

@Service
public class TemplateService {

    private final PebbleEngine pebbleEngine;

    @Autowired
    public TemplateService(PebbleEngine pebbleEngine) {
        this.pebbleEngine = pebbleEngine;
    }

    public String evaluate(String template, Map<String, Object> values) {
        notNull(template);
        notNull(values);
        try (Writer writer = new StringWriter()) {
            PebbleTemplate pebbleTemplate = pebbleEngine.getTemplate(template);
            pebbleTemplate.evaluate(writer, values);
            return writer.toString();
        } catch (PebbleException | IOException e) {
            throw new TemplateException(e);
        }
    }
}
