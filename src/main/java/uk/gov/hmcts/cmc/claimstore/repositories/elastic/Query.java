package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.Objects;

public class Query {

    private final QueryBuilder queryBuilder;

    public Query(QueryBuilder queryBuilder) {
        Objects.requireNonNull(queryBuilder, "QueryBuilder cannot be null in search");
        this.queryBuilder = queryBuilder;
    }

    @Override
    public String toString() {
        return "{" + "\"query\": " +
            queryBuilder.toString() +
            '}';
    }
}
