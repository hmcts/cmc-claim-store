package uk.gov.hmcts.cmc.rpa.mapper.json;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A {@link JsonObjectBuilder} implementation that accepts null values in builder methods.
 */
public class NullAwareJsonObjectBuilder implements JsonObjectBuilder {

    private final JsonObjectBuilder delegate;

    public NullAwareJsonObjectBuilder() {
        delegate = JsonProvider.provider().createObjectBuilder();
    }

    @Override
    public JsonObjectBuilder add(String name, JsonValue value) {
        delegate.add(name, value != null ? value : JsonObject.NULL);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, String value) {
        delegate.add(name, value != null ? Json.createValue(value) : JsonObject.NULL);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, BigInteger value) {
        delegate.add(name, value != null ? Json.createValue(value) : JsonObject.NULL);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, BigDecimal value) {
        delegate.add(name, value != null ? Json.createValue(value) : JsonObject.NULL);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, int value) {
        delegate.add(name, value);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, long value) {
        delegate.add(name, value);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, double value) {
        delegate.add(name, value);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, boolean value) {
        delegate.add(name, value);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, JsonObjectBuilder builder) {
        delegate.add(name, builder);
        return this;
    }

    @Override
    public JsonObjectBuilder add(String name, JsonArrayBuilder builder) {
        delegate.add(name, builder);
        return this;
    }

    @Override
    public JsonObjectBuilder addNull(String name) {
        delegate.addNull(name);
        return this;
    }

    @Override
    public JsonObject build() {
        return delegate.build();
    }
}
