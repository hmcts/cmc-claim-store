package uk.gov.hmcts.cmc.ccd.migration.config;

import org.skife.jdbi.v2.ContainerBuilder;
import org.skife.jdbi.v2.tweak.ContainerFactory;

import java.util.Optional;

/**
 * Courtesy of Dropwizard: https://github.com/dropwizard/dropwizard/blob/master/dropwizard-jdbi/src/main/java/io/dropwizard/jdbi/OptionalContainerFactory.java
 */
public class OptionalContainerFactory implements ContainerFactory<Optional<?>> {

    @Override
    public boolean accepts(Class<?> type) {
        return Optional.class.isAssignableFrom(type);
    }

    @Override
    public ContainerBuilder<Optional<?>> newContainerBuilderFor(Class<?> type) {
        return new OptionalContainerBuilder();
    }

    private static class OptionalContainerBuilder implements ContainerBuilder<Optional<?>> {

        private Optional<?> optional = Optional.empty();

        @Override
        public ContainerBuilder<Optional<?>> add(Object it) {
            optional = Optional.ofNullable(it);
            return this;
        }

        @Override
        public Optional<?> build() {
            return optional;
        }
    }
}
